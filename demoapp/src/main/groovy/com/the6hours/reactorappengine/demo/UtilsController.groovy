package com.the6hours.reactorappengine.demo

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

/**
 *
 * Since 28.08.13
 * @author Igor Artamonov, http://igorartamonov.com
 */
@Controller
class UtilsController {

    @RequestMapping('/')
    @ResponseBody
    String index() {
        return ["Welcome!!!", " * /simple/local", " * /simple/queue"].join('<br/>\n')
    }
}
