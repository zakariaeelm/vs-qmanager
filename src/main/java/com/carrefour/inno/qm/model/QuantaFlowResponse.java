package com.carrefour.inno.qm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuantaFlowResponse {

    public int zoneId;
    public int agregateValue;
    public int occupencyValue;
    public Date aggregateDate;

    public QuantaFlowResponse() {
    }

    public int getZoneId() {
        return zoneId;
    }

    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }

    public int getAgregateValue() {
        return agregateValue;
    }

    public void setAgregateValue(int agregateValue) {
        this.agregateValue = agregateValue;
    }

    public int getOccupencyValue() {
        return occupencyValue;
    }

    public void setOccupencyValue(int occupencyValue) {
        this.occupencyValue = occupencyValue;
    }

    public Date getAggregateDate() {
        return aggregateDate;
    }

    public void setAggregateDate(Date aggregateDate) {
        this.aggregateDate = aggregateDate;
    }
}
