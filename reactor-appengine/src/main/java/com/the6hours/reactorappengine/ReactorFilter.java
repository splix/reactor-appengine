package com.the6hours.reactorappengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Since 08.09.13
 *
 * @author Igor Artamonov, http://igorartamonov.com
 */
@Component("reactorFilter")
public class ReactorFilter extends OncePerRequestFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ReactorFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            log.debug("Check Executor Service...");
            CurrentRequestExecutorService executorService = CurrentRequestExecutorService.getInstance();
            if (!executorService.isTerminated()) {
                log.debug("Shutdown Executor Service...");
                try {
                    executorService.shutdown();
                    executorService.awaitTermination(5, TimeUnit.SECONDS);
                    log.debug("Executor Service Shutdown");
                } catch (InterruptedException e) {
                    log.error("Current Request ExecutorService termination was interrupted", e);
                }
            }
        }
    }
}
