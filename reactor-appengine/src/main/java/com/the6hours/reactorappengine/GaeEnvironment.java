package com.the6hours.reactorappengine;

import reactor.core.Environment;
import reactor.core.configuration.PropertiesConfigurationReader;
import reactor.event.dispatch.Dispatcher;

import java.util.Collections;
import java.util.List;

/**
 * Since 08.09.13
 *
 * @author Igor Artamonov, http://igorartamonov.com
 */
public class GaeEnvironment extends Environment {

    public GaeEnvironment() {
        super(Collections.<String, List<Dispatcher>>emptyMap(), new PropertiesConfigurationReader(), null);
    }

}
