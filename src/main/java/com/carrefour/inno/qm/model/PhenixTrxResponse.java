package com.carrefour.inno.qm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PhenixTrxResponse {

    private int totalTrxScanNbr;
    private List<PhenixTrxResponseData> data;

    public PhenixTrxResponse(int totalTrxScanNbr, List<PhenixTrxResponseData> data) {
        this.totalTrxScanNbr = totalTrxScanNbr;
        this.data = data;
    }

    public PhenixTrxResponse() {
    }

    public PhenixTrxResponse(int totalTrxScanNbr) {
        this.totalTrxScanNbr = totalTrxScanNbr;
    }

    public int getTotalTrxScanNbr() {
        return totalTrxScanNbr;
    }

    public void setTotalTrxScanNbr(int totalTrxScanNbr) {
        this.totalTrxScanNbr = totalTrxScanNbr;
    }

    public List<PhenixTrxResponseData> getData() {
        return data;
    }

    public void setData(List<PhenixTrxResponseData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PhenixTrxResponse{" +
                "totalTrxScanNbr=" + totalTrxScanNbr +
                ", data=" + data +
                '}';
    }
}