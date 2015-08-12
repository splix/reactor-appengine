package com.the6hours.reactorappengine;

import reactor.Environment;
import reactor.core.config.PropertiesConfigurationReader;
import reactor.core.Dispatcher;

import java.util.Collections;
import java.util.List;

/**
 * Since 08.09.13
 *
 * @author Igor Artamonov, http://igorartamonov.com
 */
public class GaeEnvironment extends Environment {

    public GaeEnvironment() {
        super(Collections.<String, Dispatcher>emptyMap(), new PropertiesConfigurationReader());
    }

}
