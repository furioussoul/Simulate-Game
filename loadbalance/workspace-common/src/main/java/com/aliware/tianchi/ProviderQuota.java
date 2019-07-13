package com.aliware.tianchi;

public enum ProviderQuota {
    INSTANCE;

    public int maxTaskCount;
    public int activeTaskCount;
    public double cpuMetric;

    ProviderQuota() {

    }


    @Override
    public String toString() {
        return "{" +
                "maxTaskCount:" + maxTaskCount +
                ", activeTaskCount:" + activeTaskCount +
                ", cpuMetric:" + cpuMetric +
                '}';
    }
}