package com.carrefour.inno.qm.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="store")
public class Store {

    @Id
    private String id;
    private String storeEan;
    private String ppsf;
    private String storeDesc;
    private String capacity;
    private String lastUpdate;
    private String currentState;
    private String customerByTrx;
    private String refreshInterval;
    private String format;

    public Store() {}

    public Store(String storeEan, String storeDesc, String capacity) {
        this.storeEan = storeEan;
        this.storeDesc = storeDesc;
        this.capacity = capacity;
    }

    public Store(String id, String storeEan, String storeDesc, String capacity, String lastUpdate, String currentState, String customerByTrx, String refreshInterval) {
        this.id = id;
        this.storeEan = storeEan;
        this.storeDesc = storeDesc;
        this.capacity = capacity;
        this.lastUpdate = lastUpdate;
        this.currentState = currentState;
        this.customerByTrx = customerByTrx;
        this.refreshInterval = refreshInterval;
    }
    public Store(String storeEan, String storeDesc, String capacity, String lastUpdate, String currentState, String customerByTrx, String refreshInterval) {
        this.storeEan = storeEan;
        this.storeDesc = storeDesc;
        this.capacity = capacity;
        this.lastUpdate = lastUpdate;
        this.currentState = currentState;
        this.customerByTrx = customerByTrx;
        this.refreshInterval = refreshInterval;
    }

    public Store(String id, String storeEan, String ppsf, String storeDesc, String capacity, String lastUpdate, String currentState, String customerByTrx, String refreshInterval) {
        this.id = id;
        this.storeEan = storeEan;
        this.ppsf = ppsf;
        this.storeDesc = storeDesc;
        this.capacity = capacity;
        this.lastUpdate = lastUpdate;
        this.currentState = currentState;
        this.customerByTrx = customerByTrx;
        this.refreshInterval = refreshInterval;
    }

    public Store(String ppsf, String storeEan, StoreDTO storeDTO) {
        this.ppsf = ppsf;
        this.storeEan = storeEan;
        this.storeDesc = storeDTO.getLocationName();
        this.format = storeDTO.getFormat();
    }

    public String getStoreEan() {
        return storeEan;
    }

    public void setStoreEan(String storeEan) {
        this.storeEan = storeEan;
    }

    public String getStoreDesc() {
        return storeDesc;
    }

    public void setStoreDesc(String storeDesc) {
        this.storeDesc = storeDesc;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public String getCustomerByTrx() {
        return customerByTrx;
    }

    public void setCustomerByTrx(String customerByTrx) {
        this.customerByTrx = customerByTrx;
    }

    public String getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(String refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public boolean incrementCountBy(int step) {
            int state = Integer.parseInt(currentState);
            currentState = String.valueOf(state + step);

            return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPpsf() {
        return ppsf;
    }

    public void setPpsf(String ppsf) {
        this.ppsf = ppsf;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return "Store{" +
                "id='" + id + '\'' +
                ", storeEan='" + storeEan + '\'' +
                ", ppsf='" + ppsf + '\'' +
                ", storeDesc='" + storeDesc + '\'' +
                ", capacity='" + capacity + '\'' +
                ", lastUpdate='" + lastUpdate + '\'' +
                ", currentState='" + currentState + '\'' +
                ", customerByTrx='" + customerByTrx + '\'' +
                ", refreshInterval='" + refreshInterval + '\'' +
                '}';
    }
}