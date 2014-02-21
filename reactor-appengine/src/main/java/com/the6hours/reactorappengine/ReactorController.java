package com.the6hours.reactorappengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.composable.Deferred;
import reactor.core.composable.Promise;
import reactor.core.composable.spec.Promises;
import reactor.event.Event;
import reactor.function.Consumer;

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

    @Autowired
    @Qualifier("reactor")
    private Reactor reactor;

    @Autowired
    @Qualifier("reactorEnv")
    private Environment environment;

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

        Deferred<Event, Promise<Event>> deferred = Promises.<Event>defer()
                .env(environment)
                .dispatcher(reactor.getDispatcher())
                .get();

        final boolean[] failed = new boolean[] {false};

        Consumer<Throwable> onError = new Consumer<Throwable>() {
            @Override
            public void accept(Throwable t) {
                log.error("Error during execution: " + t.getMessage());
                failed[0] = true;
            }
        };

        Event event = new Event<Object>(command.getHeaders(), command.getData(), onError);
        log.debug("Executing event on local Reactor " + event.getId());
        reactor.notify(command.getKey(), event, deferred);

        Promise<Event> promise = deferred.compose();
        promise.await(10*60 - 10, TimeUnit.SECONDS); //little bit less than 10 minutes (a lifetime for a Queue request)
//        log.info("Promise " + promise.isSuccess() + " for " + event.getId());
//        log.info("Failed " + failed[0] + " for " + event.getId());

        HttpStatus status =  promise.isSuccess() && !failed[0] ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        if (promise.isError() || failed[0]) {
            log.warn("Failed to process current event. Return status 500 " + event.getId());
        } else {
            log.debug("Successfully finished processing of an event " + event.getId());
        }

        response.setStatus(status.value());
        response.setContentType("text/plain");
        response.getWriter().print("PROCESSED");
    }

}
