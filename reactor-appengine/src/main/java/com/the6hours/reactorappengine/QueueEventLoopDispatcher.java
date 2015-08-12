package com.the6hours.reactorappengine;

import com.google.appengine.api.taskqueue.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.bus.Event;
import reactor.core.Dispatcher;
import reactor.core.dispatch.InsufficientCapacityException;
import reactor.fn.Consumer;

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
    public void forceShutdown() {
    }

    @Override
    public void shutdown() {
    }

    byte[] toBytes(ReactorCommand msg, Consumer<Throwable> errorConsumer) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] msgBytes = new byte[] {};
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(msg);
            msgBytes = bos.toByteArray();
        } catch (IOException e) {
            log.error("Failed", e);
            errorConsumer.accept(e);
            return null;
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
        return msgBytes;
    }

    byte[] serializeEvent(Event event) {
        ReactorCommand msg = new ReactorCommand();
        msg.setHeaders(event.getHeaders());
        msg.setKey((Serializable) event.getKey());
        msg.setData((Serializable) event.getData());

        return toBytes(msg, event.getErrorConsumer());
    }

    byte[] serializeObject(Serializable data, Consumer<Throwable> errorConsumer) {
        ReactorCommand msg = new ReactorCommand();
        msg.setData(data);
        return toBytes(msg, errorConsumer);
    }

    @Override
    public <E> void dispatch(E event, Consumer<E> eventConsumer, Consumer<Throwable> errorConsumer) {
        byte[] msgBytes;

        if (event instanceof Event) {
            msgBytes = serializeEvent((Event) event);
        } else {
            msgBytes = serializeObject((Serializable) event, errorConsumer);
        }

        if (msgBytes == null) {
            log.warn("Data is not serialized. Ignore rest");
            return;
        }

        Queue queue = QueueFactory.getDefaultQueue();
        try {
            TaskHandle th = queue.add(TaskOptions.Builder.withUrl("/_ah/reactor")
                            .payload(msgBytes)
//                            .taskName(event.getId().toString())
            );
        } catch (TaskAlreadyExistsException e) {
            log.debug("Already sent task");
        } catch (Throwable t) {
            log.error("Cannot send task");
            errorConsumer.accept(t);
            return;
        }
        eventConsumer.accept(event);
    }

    @Override
    public <E> void tryDispatch(E data, Consumer<E> eventConsumer, Consumer<Throwable> errorConsumer) throws InsufficientCapacityException {
        dispatch(data, eventConsumer, errorConsumer);
    }

    @Override
    public long remainingSlots() {
        return 100; //TODO
    }

    @Override
    public long backlogSize() {
        return 100; //TODO
    }

    @Override
    public boolean supportsOrdering() {
        return false;
    }

    @Override
    public boolean inContext() {
        return false;
    }

    @Override
    public void execute(Runnable command) {
        throw new NoSuchMethodError("Cannot execute Runnable using TaskQueue");
    }
}
