package com.carrefour.inno.qm.model;

public class Count {

    private String letEnter;
    private String totalCustomersIn;
    private String capacity;

    public Count(String letEnter, String totalCustomersIn, String capacity) {
        this.letEnter = letEnter;
        this.totalCustomersIn = totalCustomersIn;
        this.capacity = capacity;
    }

    public String getLetEnter() {
        return letEnter;
    }

    public void setLetEnter(String letEnter) {
        this.letEnter = letEnter;
    }

    public String getTotalCustomersIn() {
        return totalCustomersIn;
    }

    public void setTotalCustomersIn(String totalCustomersIn) {
        this.totalCustomersIn = totalCustomersIn;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }
}
