package com.example.salesmanagement;

import java.time.LocalDate;

public class TemporalChartData {
    private LocalDate key;
    private Number value;

    public TemporalChartData(LocalDate key, Number value) {
        this.key = key;
        this.value = value;
    }

    public LocalDate getKey() {
        return key;
    }

    public void setKey(LocalDate key) {
        this.key = key;
    }

    public Number getValue() {
        return value;
    }

    public void setValue(Number value) {
        this.value = value;
    }
}
