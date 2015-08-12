package com.the6hours.reactorappengine;

import reactor.bus.Event;

import java.io.Serializable;

/**
*
* @author Igor Artamonov
*/
class ReactorCommand implements Serializable {

    private Serializable key;
    private Event.Headers headers;
    private Serializable data;

    public Serializable getKey() {
        return key;
    }

    public void setKey(Serializable key) {
        this.key = key;
    }

    public Event.Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Event.Headers headers) {
        this.headers = headers;
    }

    public Serializable getData() {
        return data;
    }

    public void setData(Serializable data) {
        this.data = data;
    }

}
