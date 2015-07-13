package emr.analytics.models.structures;

import org.junit.Assert;
import org.junit.Test;

public class CircularQueueTest {

    @Test
    public void testAdd() throws Exception {

        CircularQueue<Integer> queue = new CircularQueue<Integer>(Integer.class, 20);
        Assert.assertEquals("The queue's size should be 0.", 0, queue.getSize());
        Assert.assertNull("The last value should be null.", queue.getLast());

        queue.add(1);
        Assert.assertEquals("The queue's size should be 1.", 1, queue.getSize());
        Assert.assertEquals("The last value should be 1.", (Integer) 1, queue.getLast());
        Assert.assertArrayEquals("The list should be equal to [1].", new Integer[]{1}, queue.getList().toArray());

        queue.add(2);
        Assert.assertEquals("The queue's size should be 2.", 2, queue.getSize());
        Assert.assertEquals("The last value should be 2.", (Integer) 2, queue.getLast());
        Assert.assertArrayEquals("The list should be equal to [1, 2].", new Integer[]{1, 2}, queue.getList().toArray());

        queue.add(new Integer[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
        Assert.assertEquals("The queue's size should be 20.", 20, queue.getSize());
        Assert.assertEquals("The last value should be 20.", (Integer) 20, queue.getLast());
        Assert.assertArrayEquals("The list should be equal to [1:20].",
                new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20},
                queue.getList().toArray());

        queue.add(21);
        Assert.assertEquals("The queue's size should be 20.", 20, queue.getSize());
        Assert.assertEquals("The last value should be 21.", (Integer) 21, queue.getLast());
        Assert.assertArrayEquals("The list should be equal to [2:21].",
                new Integer[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21},
                queue.getList().toArray());
    }
}