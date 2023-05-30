package eth.epieffe.test.vmlens;

import com.vmlens.api.AllInterleavings;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;

public class ConcurrentMapTests {

    public void updateWrong(ConcurrentHashMap<Integer, Integer> map) {
        Integer result = map.get(1);
        if (result == null) {
            map.put(1, 1);
        } else {
            map.put(1, result + 1);
        }
    }

    public void updateCorrect(ConcurrentHashMap<Integer, Integer> map) {
        map.merge(1, 1, Integer::sum);
    }

    @Test
    public void testWrong() throws InterruptedException {
        try (AllInterleavings allInterleavings = new AllInterleavings("Update Map Wrong");) {
            while (allInterleavings.hasNext()) {

                var map = new ConcurrentHashMap<Integer, Integer>();

                Thread first = new Thread(() -> updateWrong(map));
                Thread second = new Thread(() -> updateWrong(map));

                first.start();
                second.start();

                first.join();
                second.join();

                assertEquals(2,map.get(1).intValue());
            }
        }
    }

    @Test
    public void demoRight() throws InterruptedException {
        try (AllInterleavings allInterleavings = new AllInterleavings("Update Map Right");) {
            while (allInterleavings.hasNext()) {

                var map = new ConcurrentHashMap<Integer, Integer>();

                Thread first = new Thread(() -> updateCorrect(map));
                Thread second = new Thread(() -> updateCorrect(map));

                first.start();
                second.start();

                first.join();
                second.join();

                assertEquals(2,map.get(1).intValue());
            }
        }
    }
}
