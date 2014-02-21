package com.the6hours.reactorappengine;

import reactor.event.Event;
import reactor.event.dispatch.BaseLifecycleDispatcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Since 09.09.13
 *
 * @author Igor Artamonov, http://igorartamonov.com
 */
public class CurrentRequestDispatcher extends BaseLifecycleDispatcher {

    private final ExecutorService executor;

    public CurrentRequestDispatcher() {
        this.executor = CurrentRequestExecutorService.getInstance();
    }

    @Override
    public boolean awaitAndShutdown(long timeout, TimeUnit timeUnit) {
        shutdown();
        try {
            return executor.awaitTermination(timeout, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        super.shutdown();
    }

    @Override
    public void halt() {
        executor.shutdownNow();
        super.halt();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <E extends Event<?>> Task<E> createTask() {
        return (Task<E>) new ThreadPoolTask();
    }

    private class ThreadPoolTask extends Task<Event<Object>> implements Runnable {
        @Override
        public void submit() {
            executor.submit(this);
        }

        @Override
        public void run() {
            execute();
        }
    }

}
