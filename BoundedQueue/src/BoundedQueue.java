import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by michellecao on 4/30/16.
 */
public class BoundedQueue {
    Queue<Integer> queue = new LinkedList<Integer>();
    private final int bound = 3;

    ReentrantLock lock = new ReentrantLock();
    Condition waitOnEmpty = lock.newCondition();
    Condition waitOnFull = lock.newCondition();

    public static boolean isShuttingDown = false;

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void shutdown() {
        System.out.println("shutting down");
        lock.lock();
        try {
            this.waitOnEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void put(int element) {
        lock.lock();
        try {
            while(queue.size() == bound) {
                waitOnFull.await();
            }
            queue.offer(element);
            waitOnEmpty.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void putBatch(List<Integer> batch){
        lock.lock();
        try {
            while(queue.size() > (bound - batch.size())) {
                waitOnFull.await();
            }
            for(int i = 0; i < batch.size(); i++) {
                queue.offer(batch.get(i));
            }
            waitOnEmpty.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public int get() {
        lock.lock();
        int result = Integer.MIN_VALUE;
        try {
            while(queue.isEmpty() && !isShuttingDown) {
                waitOnEmpty.await();
            }
            if (!queue.isEmpty()) {
                result = queue.poll();
                waitOnFull.signal();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return result;
    }
}
