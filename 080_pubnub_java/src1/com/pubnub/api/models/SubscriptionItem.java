package com.pubnub.api.models;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter()
@Getter
@Accessors(chain = true)
public class SubscriptionItem {

    private String name;
    private Object state;

}
