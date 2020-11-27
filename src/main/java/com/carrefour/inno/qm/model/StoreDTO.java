package com.carrefour.inno.qm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class StoreDTO {

    private String locationName;
    private String format;
    @JsonIgnore
    private Object additionalPartyIdentifications;

    public StoreDTO() {
    }

    public StoreDTO(String locationName, String format, Object additionalPartyIdentifications) {
        this.locationName = locationName;
        this.format = format;
        this.additionalPartyIdentifications = additionalPartyIdentifications;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Object getAdditionalPartyIdentifications() {
        return additionalPartyIdentifications;
    }

    public void setAdditionalPartyIdentifications(Object additionalPartyIdentifications) {
        this.additionalPartyIdentifications = additionalPartyIdentifications;
    }
}
