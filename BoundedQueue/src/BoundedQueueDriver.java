import java.util.LinkedList;
import java.util.List;

/**
 * Created by michellecao on 4/30/16.
 */

public class BoundedQueueDriver implements Runnable {
    BoundedQueue queue;
    String type;
    public BoundedQueueDriver(BoundedQueue queue, String type) {
        this.queue = queue;
        this.type = type;
    }

    public void run() {
        long currentThreadId = Thread.currentThread().getId();
        System.out.println("Hello from a " + type + "thread");

        if (type.equals("put")) {
            for(int i = 0; i < 10; i++) {
                System.out.println("Attemp to PUT: " + i + " threadId: " + currentThreadId);
                queue.put(i);
                System.out.println("Done w/ PUT: " + i + " threadId: " + currentThreadId);
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (type.equals("putBatch")) {

            System.out.println(queue.get() + " GET threadId: " + currentThreadId);

            List<Integer> batch = new LinkedList<Integer>();
            batch.add(2);
            batch.add(3);
            batch.add(4);
            queue.putBatch(batch);
            System.out.println("PUTBATCH 2,3,4 threadId: " + currentThreadId);

        } else if (type.equals("get")) {
            while(!BoundedQueue.isShuttingDown) {
                System.out.println("Attempt GET threadId: " + currentThreadId);
                int result = queue.get();
                if (!queue.isEmpty()) {
                    System.out.println("Done GET " + result + " GET threadId: " + currentThreadId);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        BoundedQueue bq = new BoundedQueue();

        Thread putThread0 = new Thread(new BoundedQueueDriver(bq, "put"));
        Thread putThread1 = new Thread(new BoundedQueueDriver(bq, "putBatch"));
        Thread putThread2 = new Thread(new BoundedQueueDriver(bq, "get"));
        putThread0.start();
        putThread1.start();
        putThread2.start();
        putThread0.join();//wait for all threads to complete
        putThread1.join();

        BoundedQueue.isShuttingDown = true;
        bq.shutdown();
        putThread2.join();
    }
}
