package com.carrefour.inno.qm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuantaFlowToken {

    @JsonProperty("Usertoken")
    private String userToken;

    public QuantaFlowToken() {
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
}