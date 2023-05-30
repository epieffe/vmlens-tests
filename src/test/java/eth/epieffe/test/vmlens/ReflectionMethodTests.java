package eth.epieffe.test.vmlens;

import com.vmlens.api.AllInterleavings;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReflectionMethodTests {

    public static class MyObject {
        private String myField;
        public String getMyField() { return myField; }
        public void setMyField(String myField) { this.myField = myField; }
    }

    @Test
    public void testInvokeSharedMethod() throws InterruptedException, NoSuchMethodException {
        try (var allInterleavings = new AllInterleavings("Invoke Shared Method");) {
            while (allInterleavings.hasNext()) {
                Method method = MyObject.class.getMethod("setMyField", String.class);
                var object1 = new MyObject();
                var object2 = new MyObject();

                // Invoke the same Method instance on two different objects
                Thread t1 = new Thread(() -> {
                    try {
                        method.invoke(object1, "VALUE1");
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
                Thread t2 = new Thread(() -> {
                    try {
                        method.invoke(object2, "VALUE2");
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });

                t1.start();
                t2.start();
                t1.join();
                t2.join();

                assertEquals("VALUE1", object1.getMyField());
                assertEquals("VALUE2", object2.getMyField());
            }
        }
    }

    @Test
    public void testInvokeMethods() throws InterruptedException, NoSuchMethodException {
        try (var allInterleavings = new AllInterleavings("Invoke Methods");) {
            while (allInterleavings.hasNext()) {
                Method method1 = MyObject.class.getMethod("setMyField", String.class);
                Method method2 = MyObject.class.getMethod("setMyField", String.class);
                assertTrue(method1 != method2);

                var object1 = new MyObject();
                var object2 = new MyObject();

                // Invoke the same Method instance on two different objects
                Thread t1 = new Thread(() -> {
                    try {
                        method1.invoke(object1, "VALUE1");
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });

                Thread t2 = new Thread(() -> {
                    try {
                        method2.invoke(object2, "VALUE2");
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });

                t1.start();
                t2.start();
                t1.join();
                t2.join();

                assertEquals("VALUE1", object1.getMyField());
                assertEquals("VALUE2", object2.getMyField());
            }
        }
    }
}
