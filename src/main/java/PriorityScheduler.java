import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.internal.schedulers.NewThreadWorker;
import rx.internal.schedulers.SchedulerLifecycle;
import rx.internal.util.RxThreadFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class PriorityScheduler implements SchedulerLifecycle {
    public PriorityScheduler(int maxQueueSize, int maxThreads) {
        this(maxQueueSize, maxThreads, new RxThreadFactory("PriorityScheduler"));
    }

    public PriorityScheduler(int maxQueueSize, int maxThreads, ThreadFactory threadFactory) {
        this.maxQueueSize = maxQueueSize;
        this.maxThreads = maxThreads;
        this.threadFactory = threadFactory;
        this.pool = new AtomicReference<FixedSchedulerPool>(NONE);
        queue = new BoundedPriorityBlockingQueue<>(maxQueueSize);
        start();
    }

    private BoundedPriorityBlockingQueue<PriorityAction> queue;


    int maxThreads = 0;
    int maxQueueSize;


    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }


    static final FixedSchedulerPool NONE = new FixedSchedulerPool(null, 0);

    final ThreadFactory threadFactory;
    final AtomicReference<FixedSchedulerPool> pool;


    Scheduler withPriority(int priority) {
        return new VirtualScheduler(priority);
    }


    @Override
    public void start() {
        FixedSchedulerPool update = new FixedSchedulerPool(threadFactory, maxThreads);
        if (!pool.compareAndSet(NONE, update)) {
            update.shutdown();
        } else {

            for (PoolWorker worker : pool.get().getAllWorkers()) {
                worker.scheduleActual(new Action0() {
                    @Override
                    public void call() {
                        while (true) {
                            if (!queue.isEmpty()) {
                                synchronized (queue) {
                                    if (!queue.isEmpty()) {
                                        queue.remove().call();
                                    }
                                }
                            } else {
                                //wait
                            }
                        }
                    }
                }, 0, TimeUnit.SECONDS);
            }

        }
    }

    @Override
    public void shutdown() {
        for (; ; ) {
            FixedSchedulerPool curr = pool.get();
            if (curr == NONE) {
                return;
            }
            if (pool.compareAndSet(curr, NONE)) {
                curr.shutdown();
                return;
            }
        }
    }


    static final class PoolWorker extends NewThreadWorker {
        PoolWorker(ThreadFactory threadFactory) {
            super(threadFactory);
        }
    }

    static final class FixedSchedulerPool {
        final int maxThreads;

        final PoolWorker[] workers;
        long n;

        FixedSchedulerPool(ThreadFactory threadFactory, int maxThreads) {
            // initialize event loops
            this.maxThreads = maxThreads;
            this.workers = new PoolWorker[maxThreads];
            for (int i = 0; i < maxThreads; i++) {
                this.workers[i] = new PoolWorker(threadFactory);
            }
        }

        public List<PoolWorker> getAllWorkers() {
            return Arrays.asList(workers);
        }

        public void shutdown() {
            for (PoolWorker w : workers) {
                w.unsubscribe();
            }
        }
    }

    private class VirtualScheduler extends Scheduler {
        private int priority;

        public VirtualScheduler(int priority) {
            this.priority = priority;
        }

        @Override
        public Worker createWorker() {
            return new VirtualWorker(priority);
        }
    }

    private class VirtualWorker extends Scheduler.Worker {
        private Action0 scheduledAction;
        private int priority;
        private boolean unsubscribed = false;

        VirtualWorker(int priority) {
            this.priority = priority;
        }

        @Override
        public void unsubscribe() {
            if (scheduledAction != null) {
                PriorityScheduler.this.queue.remove(scheduledAction);
                unsubscribed = true;
            }
        }

        @Override
        public boolean isUnsubscribed() {
            return unsubscribed;
        }

        @Override
        public Subscription schedule(final Action0 action) {
            PriorityAction wrapped = new PriorityAction(action, priority);
            queue.add(wrapped);
            return wrapped;
        }

        @Override
        public Subscription schedule(final Action0 action, long delayTime, TimeUnit unit) {
            throw new IllegalArgumentException("Method not allowed.");
        }
    }

    public class PriorityAction implements Comparable<PriorityAction>, Action0, Subscription {
        private int priority;
        private Action0 action;
        private boolean unsubscribed = false;

        PriorityAction(Action0 action, int priority) {
            this.action = action;
            this.priority = priority;
        }

        @Override
        public void call() {
            action.call();
        }

        @Override
        public int compareTo(PriorityAction o) {
            return priority - o.priority;
        }

        @Override
        public boolean isUnsubscribed() {
            return unsubscribed;
        }

        @Override
        public void unsubscribe() {
            queue.remove(this);
            unsubscribed = true;
        }


    }
}