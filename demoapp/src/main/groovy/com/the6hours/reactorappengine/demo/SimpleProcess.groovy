package com.the6hours.reactorappengine.demo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.bus.Event
import reactor.spring.context.annotation.Selector

/**
 *
 * Since 28.08.13
 * @author Igor Artamonov, http://igorartamonov.com
 */
@Component
class SimpleProcess {

    private static final Logger log = LoggerFactory.getLogger(this)

    Random random = new Random()

    @Selector(value="simple", eventBus="@eventBus")
    public void handle(Event event) {
        log.info("Received event 'simple'")
        Thread curr = Thread.currentThread()
        String msg = [
                "Data: $event.data",
                "Id $event.id",
                "Thread: ${curr.id} ${curr.name}",
                "Headers: " + event.headers.asMap()
        ].join('\n')
        log.info("Details: $msg")
        if (random.nextInt(4) == 1) {
            log.warn("Bad luck: $event.id")
            throw new RuntimeException('Like something bad happened')
        }
    }
}
