package com.the6hours.reactorappengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.Reactor;
import reactor.event.Event;

/**
 *
 * Since 25.07.13
 * @author Igor Artamonov, http://igorartamonov.com
 */
@Component
public class ProcessReactorCommand {

    private static final Logger log = LoggerFactory.getLogger(ProcessReactorCommand.class);

    @Autowired
    @Qualifier("localReactor")
    private Reactor reactor;

    public void process(ReactorCommand command) throws Exception {
        Event event = new Event<Object>(command.getHeaders(), command.getData());
        log.debug("Executing event on local Reactor");
        reactor.notify(command.getKey(), event);
    }
}
