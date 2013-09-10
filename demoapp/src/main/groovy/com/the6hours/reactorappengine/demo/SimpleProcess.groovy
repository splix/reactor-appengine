package com.the6hours.reactorappengine.demo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.event.Event
import reactor.spring.annotation.Selector

/**
 *
 * Since 28.08.13
 * @author Igor Artamonov, http://igorartamonov.com
 */
@Component
class SimpleProcess {

    private static final Logger log = LoggerFactory.getLogger(this)

    @Selector(value="simple", reactor="@reactor")
    public void handle(Event event) {
        log.info("Received event 'simple'")
        Thread curr = Thread.currentThread()
        String msg = [
                "Data: $event.data",
                "Thread: ${curr.id} ${curr.name}",
                "Headers: " + event.headers.asMap()
        ].join('\n')
        log.info("Details: $msg")
    }
}
