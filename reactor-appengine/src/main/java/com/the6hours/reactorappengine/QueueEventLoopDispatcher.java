package com.the6hours.reactorappengine;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.event.Event;
import reactor.event.dispatch.Dispatcher;
import reactor.event.registry.Registry;
import reactor.event.routing.EventRouter;
import reactor.function.Consumer;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * Since 22.07.13
 *
 * @author Igor Artamonov, http://igorartamonov.com
 */
public class QueueEventLoopDispatcher implements Dispatcher {

    private static final Logger log = LoggerFactory.getLogger(QueueEventLoopDispatcher.class);

    @Override
    public boolean alive() {
        return true;
    }

    @Override
    public boolean awaitAndShutdown() {
        return awaitAndShutdown(1, TimeUnit.SECONDS);
    }

    @Override
    public boolean awaitAndShutdown(long timeout, TimeUnit timeUnit) {
        return true;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void halt() {
    }

    public <E extends Event<?>> boolean supports(Object key, E event) {
        if (key instanceof Serializable
                && event.getData() instanceof Serializable) {
            return true;
        }
        return false;
    }

    @Override
    public <E extends Event<?>> void dispatch(Object key,
                                              E event,
                                              Registry<Consumer<? extends Event<?>>> consumerRegistry,
                                              Consumer<Throwable> errorConsumer,
                                              EventRouter eventRouter,
                                              Consumer<E> completionConsumer) {
        ReactorCommand msg = new ReactorCommand();
        msg.setHeaders(event.getHeaders());
        msg.setKey((Serializable) key);
        msg.setData((Serializable) event.getData());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] msgBytes = new byte[] {};
        try {
          out = new ObjectOutputStream(bos);
          out.writeObject(msg);
          msgBytes = bos.toByteArray();
        } catch (IOException e) {
            log.error("Failed", e);
            //TODO impossible, but ??
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                bos.close();
            } catch (IOException e) {
                log.error("Failed", e);
            }
        }

        Queue queue = QueueFactory.getDefaultQueue();
        try {
            queue.add(TaskOptions.Builder.withUrl("/_ah/reactor")
                    //.param("id", event.getId().toString())
                    .payload(msgBytes)
            );
        } catch (TaskAlreadyExistsException e) {
            log.debug("Already sent task " + event.getId());
        }
    }

    @Override
    public <E extends Event<?>> void dispatch(E event,
                                         EventRouter eventRouter,
                                         Consumer<E> consumer,
                                         Consumer<Throwable> errorConsumer) {
        log.error("Wrong dispatch method!"); //TODO
    }

}
