package com.bitlove.fetlife.event;

public class ServiceCallCancelRequestEvent {

    private final String serviceCallAction;

    public ServiceCallCancelRequestEvent(String action) {
        this.serviceCallAction = action;
    }

    public String getServiceCallAction() {
        return serviceCallAction;
    }
}
