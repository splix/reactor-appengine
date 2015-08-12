package com.the6hours.reactorappengine;

import reactor.core.dispatch.ThreadPoolExecutorDispatcher;

/**
 * Since 09.09.13
 *
 * @author Igor Artamonov, http://igorartamonov.com
 */
public class CurrentRequestDispatcher extends ThreadPoolExecutorDispatcher {

    public static final int BACKLOG = 1000; //TODO

    public CurrentRequestDispatcher() {
        super(BACKLOG, 30, CurrentRequestExecutorService.getInstance());
    }

}
