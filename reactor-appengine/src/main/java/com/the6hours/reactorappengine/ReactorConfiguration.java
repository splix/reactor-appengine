package com.the6hours.reactorappengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.Environment;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.registry.Registry;
import reactor.bus.spec.EventBusSpec;
import reactor.core.Dispatcher;
import reactor.core.config.PropertiesConfigurationReader;
import reactor.fn.Consumer;
import reactor.spring.context.config.ConsumerBeanAutoConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Igor Artamonov, http://igorartamonov.com
 */
@Configuration
//@EnableReactor Disabled. Because tries to define Environment before this configuration
public class ReactorConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ReactorConfiguration.class);

    public static final String DISPATCHER_GAE_QUEUE = "gaeQueue";
    public static final String DISPATCHER_GAE_EXECUTOR = "gaeRequestThreadPool";

    @Bean(name = "eventBusQueue")
    public EventBus queueBus() {
        return new EventBusSpec()
                .env(getEnvironment())
                .dispatcher(DISPATCHER_GAE_QUEUE)
                .consumerRegistry(getQueueRegistry())
                .get();
    }

    @Bean(name = "eventBus")
    public EventBus localBus() {
        return new EventBusSpec()
                .env(getEnvironment())
                .dispatcher(DISPATCHER_GAE_EXECUTOR)
                .consumerRegistry(getLocalRegistry())
                .get();
    }

    @Bean(name = "reactorEnv")
    public Environment getEnvironment() {
        log.info("Initialize GaeEnvironment");
        System.setProperty("reactor.profiles.default", "appengine");
        Map<String, Dispatcher> dispatchers = new HashMap<String, Dispatcher>(2);
        dispatchers.put(DISPATCHER_GAE_QUEUE, new QueueEventLoopDispatcher());
        dispatchers.put(DISPATCHER_GAE_EXECUTOR, new CurrentRequestDispatcher());
        return new Environment(dispatchers, new PropertiesConfigurationReader());
    }

    @Bean(name = "registry")
    Registry<Object, Consumer<? extends Event<?>>> getLocalRegistry() {
        return new GaeRegistry<Object, Consumer<? extends Event<?>>>(true, false, null);
    }
    @Bean(name = "registryQueue")
    Registry<Object, Consumer<? extends Event<?>>> getQueueRegistry() {
        return new GaeRegistry<Object, Consumer<? extends Event<?>>>(true, false, null);
    }

    @Bean
    public ConsumerBeanAutoConfiguration consumerBeanAutoConfiguration() {
        return new ConsumerBeanAutoConfiguration();
    }
}
