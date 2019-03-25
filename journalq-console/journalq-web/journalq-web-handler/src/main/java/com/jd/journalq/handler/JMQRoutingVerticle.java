package com.jd.journalq.handler;

import com.jd.journalq.handler.binder.annotation.Path;
import com.jd.laf.binding.Binding;
import com.jd.laf.web.vertx.*;
import com.jd.laf.web.vertx.config.RouteConfig;
import com.jd.laf.web.vertx.config.RouteType;
import com.jd.laf.web.vertx.config.VertxConfig;
import com.jd.laf.web.vertx.lifecycle.Registrars;
import com.jd.laf.web.vertx.pool.Pool;
import com.jd.laf.web.vertx.pool.PoolFactories;
import com.jd.laf.web.vertx.pool.Poolable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.MyRoute;
import io.vertx.ext.web.impl.MyRouter;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Method;
import java.util.Map;

import static com.jd.laf.web.vertx.Environment.*;
import static com.jd.laf.web.vertx.config.VertxConfig.Builder.build;
import static com.jd.laf.web.vertx.config.VertxConfig.Builder.inherit;
import static com.jd.laf.web.vertx.handler.RenderHandler.render;

/**
 * 路由装配件
 */
public class JMQRoutingVerticle extends AbstractVerticle {
    public static final String ROUTING_CONFIG_FILE = "routing.file";
    public static final String DEFAULT_ROUTING_CONFIG_FILE = "routing.xml";
    public static final int DEFAULT_PORT = 8080;
    protected static Logger logger = LoggerFactory.getLogger(JMQRoutingVerticle.class);

    //注入的环境
    protected Environment env;
    //HTTP选项
    protected HttpServerOptions httpOptions;
    //资源文件
    protected String file;

    //配置
    protected VertxConfig config;
    protected HttpServer httpServer;
    public JMQRoutingVerticle() {
        this(new Environment.MapEnvironment(), new HttpServerOptions().setPort(DEFAULT_PORT), DEFAULT_ROUTING_CONFIG_FILE);
    }

    public JMQRoutingVerticle(final Environment env) {
        this(env, new HttpServerOptions().setPort(DEFAULT_PORT), DEFAULT_ROUTING_CONFIG_FILE);
    }

    public JMQRoutingVerticle(final Environment env, final HttpServerOptions httpOptions) {
        this(env, httpOptions, DEFAULT_ROUTING_CONFIG_FILE);
    }

    public JMQRoutingVerticle(final Environment env, final HttpServerOptions httpOptions, final String file) {
        this.env = env != null ? env : new Environment.MapEnvironment();
        this.httpOptions = httpOptions == null ? new HttpServerOptions().setPort(DEFAULT_PORT) : httpOptions;
        this.file = file != null ? file : env.getString(ROUTING_CONFIG_FILE, DEFAULT_ROUTING_CONFIG_FILE);
    }

    public JMQRoutingVerticle(final Map<String, Object> parameters) {
        this(new Environment.MapEnvironment(parameters), new HttpServerOptions().setPort(DEFAULT_PORT), DEFAULT_ROUTING_CONFIG_FILE);
    }

    public JMQRoutingVerticle(final Map<String, Object> parameters, final HttpServerOptions httpOptions) {
        this(new Environment.MapEnvironment(parameters), httpOptions, DEFAULT_ROUTING_CONFIG_FILE);
    }

    public JMQRoutingVerticle(final Map<String, Object> parameters, final HttpServerOptions httpOptions, final String file) {
        this(new Environment.MapEnvironment(parameters), httpOptions, file);
    }

    @Override
    public void start() throws Exception {
        try {
            //构建配置数据
            config = inherit(build(file));

            //初始化配置文件中的插件
            Registrars.register(vertx, env);
            //创建路由管理器
            Router router = createRouter(env);
            //构建业务处理链
            buildHandlers(router, config, env);
            //构建消息处理链
            buildConsumers(config);
            //启动服务
            httpServer = vertx.createHttpServer(httpOptions);
            httpServer.requestHandler(router::accept).listen(event -> {
                if (event.succeeded()) {
                    logger.info(String.format("success starting http server on port %d", httpServer.actualPort()));
                } else {
                    logger.error(String.format("failed starting http server on port %d",
                            httpServer.actualPort()), event.cause());
                }
            });
            logger.info(String.format("success starting routing verticle %s", deploymentID()));
        } catch (Exception e) {
            logger.error(String.format("failed starting routing verticle %s", deploymentID()), e);
            throw e;
        }
    }

    /**
     * 创建路由管理器
     *
     * @param environment 环境
     * @return
     */
    protected Router createRouter(final Environment environment) {
        return new MyRouter(vertx, environment);
    }

    @Override
    public void stop() throws Exception {
        if (httpServer != null) {
            httpServer.close(event -> {
                if (event.succeeded()) {
                    logger.info(String.format("success closing http server on port %d", httpServer.actualPort()));
                } else {
                    logger.error(String.format("failed closing http server on port %d",
                            httpServer.actualPort()), event.cause());
                }
            });
        }
        Registrars.deregister(vertx);
        logger.info(String.format("success stop routing verticle %s ", deploymentID()));
    }

    /**
     * 构造消息处理链
     *
     * @param config
     */
    protected void buildConsumers(final VertxConfig config) {
        EventBus eventBus = vertx.eventBus();
        MessageHandler handler;
        for (RouteConfig route : config.getMessages()) {
            //设置消息处理链
            for (String name : route.getHandlers()) {
                handler = MessageHandlers.getPlugin(name);
                if (handler != null && route.getPath() != null && !route.getPath().isEmpty()) {
                    eventBus.consumer(route.getPath(), handler);
                }
            }
        }
    }

    /**
     * 构造处理链
     *
     * @param router      路由
     * @param config      配置
     * @param environment 环境
     * @throws Exception
     */
    protected void buildHandlers(final Router router, final VertxConfig config, final Environment environment) throws Exception {
        Route route;
        String path;
        RouteType type;
        for (RouteConfig info : config.getRoutes()) {
            // 过滤掉模板
            if (info.isRoute()) {
                continue;
            }
            type = info.getType();
            path = info.getPath();
            path = path != null ? path.trim() : path;
            //如果没有路径，则默认路由，如果没有请求方法，则默认匹配所有请求方法
            if (path == null || path.isEmpty()) {
                route = router.route();
            } else if (!info.isRegex()) {
                route = type == null ? router.route(path) : router.route(type.getMethod(), path);
            } else {
                route = type == null ? router.routeWithRegex(path) : router.routeWithRegex(type.getMethod(), path);
            }
            if (info.getOrder() != null) {
                route.order(info.getOrder());
            }
            //设置路由配置
            if (route instanceof MyRoute) {
                ((MyRoute) route).setConfig(info);
            }
            try {
                // 设置能产生的内容
                buildProduces(route, info);
                // 设置能消费的内容
                buildConsumes(route, info);
                //设置异常处理链
                buildErrors(route, info);
                //设置业务处理链
                buildHandlers(route, info, environment);
            } catch (Exception e) {
                logger.error(String.format("build handlers error on path %s, type %s", path, type), e);
                throw e;
            }
        }
    }

    /**
     * 构建路由的异常处理
     *
     * @param route  路由对象
     * @param config 路由配置
     */
    protected void buildErrors(final Route route, final RouteConfig config) {
        if (config.getErrors() != null) {
            ErrorHandler handler;
            for (String error : config.getErrors()) {
                handler = ErrorHandlers.getPlugin(error);
                if (handler != null) {
                    route.failureHandler(handler);
                } else {
                    logger.warn(String.format("error handler %s is not found. ignore.", error));
                }
            }
        }
    }

    /**
     * 构建路由处理器
     *
     * @param route       路由对象
     * @param config      路由配置
     * @param environment 环境
     */
    protected void buildHandlers(final Route route, final RouteConfig config, final Environment environment) {
        RoutingHandler handler;
        Command command;
        //上下文处理
        for (String name : config.getHandlers()) {
                String[] commandMethod = name.split("\\.");
                handler = RoutingHandlers.getPlugin(commandMethod[0]);
                if (handler != null) {
                    if (handler instanceof RouteAware) {
                        //感知路由配置，复制一份对象，确保环境初始化的设置
                        handler = ((RouteAware) handler).clone();
                        ((RouteAware) handler).setup(config);
                    }
                    route.handler(handler);
                } else {
                    //自定义命令
                    command = Commands.getPlugin(commandMethod[0]);
                    if (command != null) {
                        Pool<Command> pool = null;
                        //判断命令是否需要池化
                        if (command instanceof Poolable) {
                            //对象池
                            int capacity = environment.getInteger(COMMAND_POOL_CAPACITY, 500);
                            int initializeSize = environment.getInteger(COMMAND_POOL_INITIALIZE_SIZE, 50);
                            if (capacity > 0) {
                                //构造对象池
                                pool = PoolFactories.getPlugin().create(capacity);
                                if (initializeSize > 0) {
                                    int min = Math.min(initializeSize, capacity);
                                    //初始化对象池大小
                                    for (int i = 0; i < min; i++) {
                                        try {
                                            pool.release(command.getClass().newInstance());
                                        } catch (InstantiationException e) {
                                        } catch (IllegalAccessException e) {
                                        }
                                    }
                                }
                            }
                        } else {
                            pool = null;
                        }
                        CommandHandler commandHandler = null;
                        if(commandMethod.length==1){
                            commandHandler = new CommandHandler(command, pool,null,null);
                        }else {
                            Method method = null;
                            for (Method md : MethodUtils.getMethodsWithAnnotation(command.getClass(),Path.class)) {
                                if(md.getAnnotation(Path.class).value().equals(commandMethod[1])) {
                                    method = md;
                                    commandHandler = new CommandHandler(command, pool,md,commandMethod[1]);
                                    break;
                                }
                            }
                            if(null==method){
                                commandHandler = new CommandHandler(command, pool,null,null);
                            }
                        }
                        route.handler(commandHandler);
                    } else {
                        logger.warn(String.format("handler %s is not found. ignore.", name));
                    }
                }
            }
    }

    /**
     * 构建消费内容
     *
     * @param route  路由对象
     * @param config 路由配置
     */
    protected void buildConsumes(final Route route, final RouteConfig config) {
        switch (config.getType()) {
            case PUT:
            case POST:
            case PATCH:
                if (config.getConsumes() != null) {
                    for (String type : config.getConsumes()) {
                        route.consumes(type);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 构建生产内容，application/json
     *
     * @param route  路由对象
     * @param config 路由配置
     */
    protected void buildProduces(final Route route, final RouteConfig config) {
        if (config.getProduces() != null) {
            for (String type : config.getProduces()) {
                route.produces(type);
            }
        }
    }

    /**
     * 命令处理器
     */
    protected static class CommandHandler implements Handler<RoutingContext> {
        //命令
        protected Command command;
        //对象池
        protected Pool<Command> pool;

        protected Method method;

        protected String path;
        public CommandHandler(Command command, Pool<Command> pool,Method method,String path) {
            this.command = command;
            this.pool = pool;
            this.method = method;
            this.path = path;
        }

        @Override
        public void handle(final RoutingContext context) {
            Command clone = null;
            try {
                //克隆一份
                clone = pool == null ? null : pool.get();
                if (clone == null) {
                    //构造新对象
                    clone = command.getClass().newInstance();
                }
                //使用环境和当前上下文进行绑定
                //理论上只有新创建的对象才需要使用环境和上下文同时绑定，对象池中的对象已经使用环境绑定过了
                //但是，有可能开发人员在清理的代码里面不小心清理了环境绑定的对象
                //避免出错，所以都重新绑定一次
                Binding.bind(context, clone);
                if(null!=method){
                    context.put(Constants.args_types,method.getGenericParameterTypes());
                    context.put(Constants.args,new Object[method.getGenericParameterTypes().length]);
                    JMQBinding.bindMethod(context, clone,method,path);
                }
                //验证
                Validates.validate(clone);
                //执行
                Object obj = null;
                if(null==method){
                    obj = clone.execute();
                }else{
                    Object[] args = context.get(Constants.args);
                    obj = method.invoke(clone,args);
                }

                Command.Result result = null;
                if (obj != null) {
                    if (obj instanceof Command.Result) {
                        result = (Command.Result) obj;
                    } else {
                        result = new Command.Result(obj);
                    }
                }
                if (result != null) {
                    //有返回结果
                    if (result.getTemplate() != null && !result.getTemplate().isEmpty()) {
                        //存放模板
                        context.put(TEMPLATE, result.getTemplate());
                    }
                    if (result.getKey() != null) {
                        //存放实际的返回结果
                        context.put(result.getKey(), result.getResult());
                    }
                    switch (result.getType()) {
                        case CONTINUE:
                            //继续
                            context.next();
                            break;
                        case END:
                            //渲染输出
                            render(context);
                            break;
                        case HOLD:
                            //挂住
                            break;
                        default:
                            //默认继续
                            context.next();
                    }
                } else {
                    context.next();
                }
            } catch (Exception e) {
                context.fail(e);
            } finally {
                if (pool != null && clone != null) {
                    pool.release(clone);
                }
            }
        }
    }
}
