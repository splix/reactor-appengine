package com.the6hours.reactorappengine.demo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *
 * Since 14.08.13
 * @author Igor Artamonov, http://igorartamonov.com
 */
@Component
class LogRequestFilter implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(this)

    @Override
    boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Thread t = Thread.currentThread()
        String details = [
                uri: request.getRequestURI(),
                thread: [t.id, t.name].join(', ')
        ].collect { [it.key, it.value].join(': ') }.join('\n')
        log.info("Request:\n$details")
        return true
    }

    @Override
    void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
