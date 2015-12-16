package com.the6hours.reactorappengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.Environment;
import reactor.bus.EventBus;
import reactor.core.Dispatcher;
import reactor.rx.Promise;
import reactor.rx.Promises;
import reactor.bus.Event;
import reactor.fn.Consumer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.concurrent.TimeUnit;

/**
 * Since 25.07.13
 *
 * @author Igor Artamonov, http://igorartamonov.com
 */
@Controller
public class ReactorController {

    private static final Logger log = LoggerFactory.getLogger(ReactorController.class);

    private static final long WAIT_TIME_SECONDS = 10 * 60 - 10; //little bit less than 10 minutes (a lifetime for a Queue request)

    @Autowired
    @Qualifier("reactorEnv")
    private Environment environment;

    @Autowired
    @Qualifier("eventBus")
    private EventBus eventBus;

    @RequestMapping(value = "/_ah/reactor")
    @ResponseBody
    public void reactor(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ObjectInput oi = new ObjectInputStream(request.getInputStream());
        ReactorCommand command;
        try {
            command = (ReactorCommand) oi.readObject();
            log.info("Process command " + command.getKey());
        } catch (ClassNotFoundException e) {
            log.error("Cannot read provided command", e);
            throw e;
        }

        Dispatcher dispatcher = environment.getDispatcher(ReactorConfiguration.DISPATCHER_GAE_EXECUTOR);

        Promise<Event> promise = Promises.<Event>ready(environment, dispatcher);

        final boolean[] failed = new boolean[] {false};

        Consumer<Throwable> onError = new Consumer<Throwable>() {
            @Override
            public void accept(Throwable t) {
                failed[0] = true;
                log.error("Error during Event processing: " + t.getMessage(), t);
            }
        };

        Event event = new Event<Object>(command.getHeaders(), command.getData(), onError);
        event.setKey(command.getKey());
        if (log.isDebugEnabled()) {
            log.debug("Executing event on local Reactor " + event.getId() + " for key " + command.getKey());
        }

        eventBus.getRouter().route(command.getKey(), event,
                eventBus.getConsumerRegistry().select(command.getKey()),
                promise, onError);

        promise.await(WAIT_TIME_SECONDS, TimeUnit.SECONDS);

        HttpStatus status =  promise.isSuccess() && !failed[0] ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        if (promise.isError() || failed[0]) {
            StringBuilder msg = new StringBuilder();
            msg.append("Failed to process current event. Return status 500 ")
                    .append(event.getId());
            Throwable t = promise.reason();
            if (t != null) {
                msg.append(". Reason: ").append(t.getMessage());
            }
            log.warn(msg.toString());
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Successfully finished processing of an event " + event.getId());
            }
        }

        response.setStatus(status.value());
        response.setContentType("text/plain");
        response.getWriter().print("PROCESSED");
    }

}
