package com.the6hours.reactorappengine.demo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import reactor.bus.Event
import reactor.bus.EventBus

/**
 *
 * Since 14.08.13
 * @author Igor Artamonov, http://igorartamonov.com
 */
@Controller
class SimpleRun {

    private static final Logger log = LoggerFactory.getLogger(this)

    @Autowired
    @Qualifier('eventBusQueue')
    EventBus eventBusQueue

    @Autowired
    @Qualifier('eventBusLocal')
    EventBus eventBusLocal

    @RequestMapping("/simple/queue")
    @ResponseBody
    String runQueue() {
        log.info("Send events to Queue Reactor")
        (1..20).each {
            eventBusQueue.notify("simple", Event.wrap("test $it"))
        }
        log.info("Sent to Queue Reactor")
        return "Sent"
    }

    @RequestMapping("/simple/local")
    @ResponseBody
    String runLocal() {
        log.info("Send events to Local Reactor")
        (1..20).each {
            eventBusLocal.notify("simple", Event.wrap("test $it"))
        }
        log.info("Sent to Local Reactor")
        return "Processed"
    }

}
