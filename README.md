Reactor Framework for Google Appengine
======================================

WARNING
-------
Currently it's just a proof of concept. In early stage of development, don't
use it in production system. And also, current realization is for Spring-based application only.


Details
-------

Allows you to use [Reactor Framework](https://github.com/reactor/reactor)
inside [GAE](https://developers.google.com/appengine/docs/java/) application.


Have two types of Dispatchers:

 * local - uses GAE ThreadManger for Current Request, for quick tasks that should be executed during current request
 * queue - process events in GAE Queue

Demo App
--------

In `demoapp` directory, there are very simple GAE application to test Reactor, and to show how it could be used. Take
a look. It also deployed into http://reactor-demo.appspot.com/

Usage
-----

### Install:
```
cd reactor-appengine
gradle install
```

### Add ReactorFilter to `web.xml`:
```
<filter>
    <filter-name>reactorFilter</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
</filter>
<filter-mapping>
    <filter-name>reactorFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

### Add Reactor Appengine beans, by adding into `spring.xml`:

```
<context:component-scan base-package="com.the6hours.reactorappengine"/>
```

### Configure event queue:

```
 <queue>
     <name>reactor</name>
     <rate>10/s</rate>
 </queue>
```

### Define Event Processor

```
@Selector(value="test", reactor="@reactor")
public void handle(Event event) {
   ...
}
```

### Use Reactor

For local execution (current request):

```
@Autowired
@Qualifier('localReactor')
Reactor localReactor

localReactor.notify("test", Event.wrap("test"))
```

For execution as a Queue Task:

```
@Autowired
@Qualifier('queueReactor')
Reactor queueReactor

queueReactor.notify("test", Event.wrap("test"))
```

Roadmap
-------

 * make it usable from non-Spring applications
 * special dispatcher for GAE Pull Queue
 * special dispatcher for GAE Backend modules

License
-------

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

Author
------

[Igor Artamonov](http://igorartamonov.com), igor@artamonov.ru

