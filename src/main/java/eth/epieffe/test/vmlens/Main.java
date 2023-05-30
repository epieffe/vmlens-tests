package eth.epieffe.test.vmlens;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Main {

    private static final Method method;

    public static class MyObject {
        private String myField;
        public String getMyField() { return myField; }
        public void setMyField(String myField) { this.myField = myField; }
    }

    static {
        try {
            method = MyObject.class.getMethod("setMyField", String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        var object = new MyObject();
        System.out.println(object.getMyField());
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; ++i) {
            invokeSetter(object);
        }
        long stop = System.currentTimeMillis();
        System.out.println(object.getMyField());
        System.out.println("Elapsed time: " + (stop - start));
    }

    public static void setPrivateField(MyObject object) throws NoSuchFieldException, IllegalAccessException {
        Field field = MyObject.class.getDeclaredField("myField");
        field.setAccessible(true);
        field.set(object, "VALUE1");
    }

    public static void invokeSetter(MyObject object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        method.invoke(object, "VALUE1");
    }

}
