package io.chubao.joyqueue.nsr.service.test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;

/**
 * @author wylixiaobin
 * Date: 2018/9/20
 */
public class VertTest {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.get("/hello").handler(routingContext -> {
            HttpServerRequest request = routingContext.request();
            System.out.println(request.params().toString());
            routingContext.response().end("helloworld");
        });
        server.requestHandler(request -> router.accept(request)).listen(8080);
        server.close();
    }
}
