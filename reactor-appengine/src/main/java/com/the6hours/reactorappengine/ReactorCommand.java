package com.the6hours.reactorappengine;

import reactor.event.Event;

import java.io.Serializable;

/**
* Since 25.07.13
*
* @author Igor Artamonov, http://igorartamonov.com
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
