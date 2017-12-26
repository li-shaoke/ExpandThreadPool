package com.lsk;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lsk10238 on 2017/12/26.
 */
public class ExtendThreadPoolExecutor extends ThreadPoolExecutor {
    public ExtendThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, WorkTaskQueue  workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,new ThreadPoolExecutor.AbortPolicy());
        workQueue.setExecutor(this);
    }

    /**
     * 计数器,用于表示当前队列里面的task的数量,这里task特指还未完成的task
     * 当task执行完后,currentTaskCount减一
     */
    private final AtomicInteger currentTaskCount = new AtomicInteger(0);


    public int getCurrentTaskCount() {
        return currentTaskCount.get();
    }


    /**
     * 覆盖父类的execute方法
     * 在任务开始执行之前,计数器加1。
     */
    @Override
    public void execute(Runnable command) {
        currentTaskCount.incrementAndGet();
        try {
            super.execute(command);
        } catch (RejectedExecutionException ex) {
            /**
             * 当发生RejectedExecutionException,尝试再次将task丢到队列里面,
             * 如果还是发生RejectedExecutionException,则直接抛出异常
             * 超过 MAX_SIZE和QUEUE_SIZE 的和，才会抛出异常
             */
            BlockingQueue<Runnable> taskQueue = super.getQueue();
            if (taskQueue instanceof WorkTaskQueue) {
                final WorkTaskQueue queue = (WorkTaskQueue)taskQueue;
                if (!queue.forceTaskIntoQueue(command)) {
                    currentTaskCount.decrementAndGet();
                    throw new RejectedExecutionException("thread pool 已经达到最大，且队列已满");
                }
            } else {
                currentTaskCount.decrementAndGet();
                throw ex;
            }
        }
    }

    /**
     * 覆盖父类的afterExecute方法
     * 当task执行完成后,将计数器减1
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        currentTaskCount.decrementAndGet();
        //super.afterExecute(r,t);  //afterExecute是空方法体，执行不执行没区别
    }
}
