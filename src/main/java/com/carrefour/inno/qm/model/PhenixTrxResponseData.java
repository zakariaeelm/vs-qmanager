package com.carrefour.inno.qm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PhenixTrxResponseData {

    private Double salAmtWtax;
    private int salUnitQty;
    private Double salUnitQtyWeight;
    private int trxNbr;
    private int trxScanNbr;

    public PhenixTrxResponseData(Double salAmtWtax, int salUnitQty, Double salUnitQtyWeight, int trxNbr, int trxScanNbr) {
        this.salAmtWtax = salAmtWtax;
        this.salUnitQty = salUnitQty;
        this.salUnitQtyWeight = salUnitQtyWeight;
        this.trxNbr = trxNbr;
        this.trxScanNbr = trxScanNbr;
    }

    public PhenixTrxResponseData() {
    }

    public Double getSalAmtWtax() {
        return salAmtWtax;
    }

    public void setSalAmtWtax(Double salAmtWtax) {
        this.salAmtWtax = salAmtWtax;
    }

    public int getSalUnitQty() {
        return salUnitQty;
    }

    public void setSalUnitQty(int salUnitQty) {
        this.salUnitQty = salUnitQty;
    }

    public Double getSalUnitQtyWeight() {
        return salUnitQtyWeight;
    }

    public void setSalUnitQtyWeight(Double salUnitQtyWeight) {
        this.salUnitQtyWeight = salUnitQtyWeight;
    }

    public int getTrxNbr() {
        return trxNbr;
    }

    public void setTrxNbr(int trxNbr) {
        this.trxNbr = trxNbr;
    }

    public int getTrxScanNbr() {
        return trxScanNbr;
    }

    public void setTrxScanNbr(int trxScanNbr) {
        this.trxScanNbr = trxScanNbr;
    }

    @Override
    public String toString() {
        return "PhenixTrxResponseData{" +
                "salAmtWtax=" + salAmtWtax +
                ", salUnitQty=" + salUnitQty +
                ", salUnitQtyWeight=" + salUnitQtyWeight +
                ", trxNbr=" + trxNbr +
                ", trxScanNbr=" + trxScanNbr +
                '}';
    }
}