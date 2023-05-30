package eth.epieffe.test.vmlens;

import com.vmlens.api.AllInterleavings;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ReflectionFieldTests {

	public static class MyObject {
		public String myField;

		public String getMyField() {
			return myField;
		}
	}

	/**
	 * Simulates two threads using two different Fields to write on two different objects.
	 * This test succeeds.
	 */
	@Test
	public void testWriteOnDifferentFields() throws InterruptedException, NoSuchFieldException {
		try (var allInterleavings = new AllInterleavings("Different Field Object");) {
			while (allInterleavings.hasNext()) {
				// Get two different Field object
				Field field1 = MyObject.class.getDeclaredField("myField");
				Field field2 = MyObject.class.getDeclaredField("myField");
				// field1 and field2 are not the same object
				assertTrue(field1 != field2);

				// Instantiate two objects
				var object1 = new MyObject();
				var object2 = new MyObject();

				// Each thread uses its own Field object
				Thread t1 = new Thread(() -> {
					try {
						field1.set(object1, "VALUE1");
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				});
				Thread t2 = new Thread(() -> {
					try {
						field2.set(object2, "VALUE2");
					} catch (IllegalAccessException e) {
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

	/**
	 * Simulates two threads using the same Field to write on two different objects.
	 * This test should fail. Its failure proves that java.lang.reflect.Field objects
	 * are not thread safe!
	 */
	@Test
	public void testWriteOnSharedField() throws InterruptedException, NoSuchFieldException {
		try (var allInterleavings = new AllInterleavings("Write on Shared Field");) {
			while (allInterleavings.hasNext()) {

				// Retrieve a Field
				Field field = MyObject.class.getDeclaredField("myField");

				// Instantiate two objects
				var object1 = new MyObject();
				var object2 = new MyObject();

				// Use the same Field object to write on different objects
				Thread t1 = new Thread(() -> {
					try {
						field.set(object1, "VALUE1");
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				});
				Thread t2 = new Thread(() -> {
					try {field.set(object2, "VALUE2");
					} catch (IllegalAccessException e) {
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
