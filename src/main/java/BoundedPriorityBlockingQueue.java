import java.util.PriorityQueue;

public class BoundedPriorityBlockingQueue<T> extends BoundedBlockingQueue<T> {
    public BoundedPriorityBlockingQueue(int capacity) {
        super(capacity, new PriorityQueue<T>());
    }
}