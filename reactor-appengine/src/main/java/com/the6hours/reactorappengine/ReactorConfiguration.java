package com.the6hours.reactorappengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.configuration.PropertiesConfigurationReader;
import reactor.core.spec.ReactorSpec;
import reactor.core.spec.Reactors;
import reactor.event.dispatch.Dispatcher;
import reactor.spring.factory.config.ConsumerBeanAutoConfiguration;

import java.util.Collections;
import java.util.List;

/**
 * Since 25.07.13
 *
 * @author Igor Artamonov, http://igorartamonov.com
 */
@Configuration
//@EnableReactor Disabled. Because tries to define Environment before this configuration
public class ReactorConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ReactorConfiguration.class);

    public static final String DISPATCHER_GAE_QUEUE = "gaeQueue";
    public static final String DISPATCHER_GAE_EXECUTOR = "gaeRequestThreadPool";

    @Bean(name = "queueReactor")
    public Reactor queueReactor() {
        Environment env = getEnvironment();
        ReactorSpec spec = Reactors.reactor().env(env);
        spec = spec.dispatcher(DISPATCHER_GAE_QUEUE);
        return spec.get();
    }

    @Bean(name = {"localReactor", "reactor", "serialReactor"})
    public Reactor localReactor() {
        Environment env = getEnvironment();
        ReactorSpec spec = Reactors.reactor().env(env);
        spec = spec.synchronousDispatcher(); //dispatcher(DISPATCHER_GAE_EXECUTOR);
        return spec.get();
    }

    @Bean(name = "reactorEnv")
    public Environment getEnvironment() {
        log.info("Initialize GaeEnvironment");
        System.setProperty("reactor.profiles.default", "appengine");
        Environment environment = new Environment(Collections.<String, List<Dispatcher>>emptyMap(), new PropertiesConfigurationReader());
        environment.addDispatcher(DISPATCHER_GAE_QUEUE, new QueueEventLoopDispatcher());
        environment.addDispatcher(DISPATCHER_GAE_EXECUTOR, new CurrentRequestDispatcher());
        return environment;
    }

    @Bean
    public ConsumerBeanAutoConfiguration consumerBeanAutoConfiguration() {
        return new ConsumerBeanAutoConfiguration();
    }
}
