import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.SpiLoader;

import org.joyqueue.plugin.ExtensionPointLazyExt;
import org.joyqueue.plugin.SingletonController;
import org.junit.Assert;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.Map;

public class SingletonForceClosingTest {

    @Test
    public void test() throws Exception{
//        Map<String,String> env=System.getenv();
//        Field field = env.getClass().getDeclaredField("m");
//        field.setAccessible(true);
//        ((Map<String, String>) field.get(env)).put("force.close.plugin.singleton","TRUE");
//        SingletonController.forceClosingSingletonClass.add(Consume.class.getName());
//        ExtensionPoint<Consume, String> CONSUME = new ExtensionPointLazyExt<>(Consume.class, SpiLoader.INSTANCE, null, null);
//        Consume c=CONSUME.get();
//        Consume cc=CONSUME.get();
//        Assert.assertNotEquals(c,cc);
    }
}
