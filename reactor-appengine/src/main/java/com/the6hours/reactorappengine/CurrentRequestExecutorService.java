package com.the6hours.reactorappengine;

import com.google.appengine.api.ThreadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Since 08.09.13
 *
 * @author Igor Artamonov, http://igorartamonov.com
 */
public class CurrentRequestExecutorService extends AbstractExecutorService {

    private static final Logger log = LoggerFactory.getLogger(CurrentRequestExecutorService.class);

    private static ThreadLocal<ExecutorService> executors = new ThreadLocal<ExecutorService>();
    private static final int size = 8;

    private static final CurrentRequestExecutorService instance = new CurrentRequestExecutorService();

    private CurrentRequestExecutorService() {
    }

    public ExecutorService getRequestExecutor() {
        ExecutorService current = executors.get();
        if (current == null) {
            log.debug("Init a new ExecutorService for current request");
            ThreadFactory threadFactory = ThreadManager.currentRequestThreadFactory();
            current = Executors.newFixedThreadPool(size, threadFactory);
            executors.set(current);
        }
        return current;
    }

    public static CurrentRequestExecutorService getInstance() {
        return instance;
    }

    public void execute(Runnable command) {
        ExecutorService e = getRequestExecutor();
        e.execute(command);
    }

    public void shutdown() {
        ExecutorService e = executors.get();
        if (e != null) {
            e.shutdown();
        }
        executors.set(null);
    }

    public List<Runnable> shutdownNow() {
        ExecutorService e = executors.get();
        if (e != null) {
            List<Runnable> runnables = e.shutdownNow();
            executors.set(null);
            return runnables;
        }
        return Collections.emptyList();
    }

    public boolean isShutdown() {
        ExecutorService e = executors.get();
        return e == null || e.isShutdown();
    }

    public boolean isTerminated() {
        ExecutorService e = executors.get();
        return e == null || e.isTerminated();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        ExecutorService e = executors.get();
        if (e == null) {
            return true;
        }
        boolean terminated = e.awaitTermination(timeout, unit);
        executors.set(null);
        return terminated;
    }

    @Override
    public Future<?> submit(Runnable task) {
        ExecutorService e = getRequestExecutor();
        return e.submit(task);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        ExecutorService e = getRequestExecutor();
        return e.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        ExecutorService e = getRequestExecutor();
        return e.submit(task, result);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        ExecutorService e = getRequestExecutor();
        return e.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                         long timeout, TimeUnit unit)
            throws InterruptedException {
        ExecutorService e = getRequestExecutor();
        return e.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        ExecutorService e = getRequestExecutor();
        return e.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                           long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService e = getRequestExecutor();
        return e.invokeAny(tasks, timeout, unit);
    }
}
