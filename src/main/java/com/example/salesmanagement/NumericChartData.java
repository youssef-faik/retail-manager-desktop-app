package com.example.salesmanagement;

public class NumericChartData {
    private Number key;
    private Number value;

    public NumericChartData(Number key, Number value) {
        this.key = key;
        this.value = value;
    }

    public Number getKey() {
        return key;
    }

    public void setKey(Number key) {
        this.key = key;
    }

    public Number getValue() {
        return value;
    }

    public void setValue(Number value) {
        this.value = value;
    }
}
