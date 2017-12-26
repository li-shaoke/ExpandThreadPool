package com.lsk;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by lsk10238 on 2017/12/26.
 */
//LinkedBlockingQueue<Runnable> 实现了 BlockingQueue<Runnable>接口，而我们不需要重写接口中的所有方法，故继承其实现类
public class WorkTaskQueue extends LinkedBlockingQueue<Runnable> {
    private ExtendThreadPoolExecutor executor;

    public WorkTaskQueue(int capacity) {
        //设置这个队列容量
        super(capacity);
    }

    public void setExecutor(ExtendThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public boolean forceTaskIntoQueue(Runnable runnable) {
        if (executor.isShutdown()) {
            throw new RejectedExecutionException("Executor已经关闭了,不能将task添加到队列里面");
        }
        return super.offer(runnable);
    }

    /**  重写 插入指定元素在这个队列的尾部 方法
     *   线程池里的线程数量已经到达最大 或 有空闲的线程  执行父类的方法处理
     *   如果线程池里的线程数量还没有到达最大,直接创建线程,而不把任务丢到队列里面
     * @param runnable
     * @return
     */
    @Override
    public  boolean offer(Runnable runnable) {
        int currentPoolThreadSize = executor.getPoolSize();
        //如果线程池里的线程数量已经到达最大,将任务添加到队列中
        if (currentPoolThreadSize == executor.getMaximumPoolSize()) {
            return super.offer(runnable);
        }
        //说明有空闲的线程,这个时候无需创建core线程之外的线程,而是把任务直接丢到队列里即可
        if (executor.getCurrentTaskCount() < currentPoolThreadSize) {
            return super.offer(runnable);
        }

        //如果线程池里的线程数量还没有到达最大,直接创建线程,而不把任务丢到队列里面
        if (currentPoolThreadSize < executor.getMaximumPoolSize()) {
            return false;
        }

        return super.offer(runnable);
    }
}
