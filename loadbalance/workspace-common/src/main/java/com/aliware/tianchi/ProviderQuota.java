package com.aliware.tianchi;

public enum ProviderQuota {
    INSTANCE;

    public String quotaName;
    public int maxTaskCount;
    public int activeTaskCount;
    public double cpuMetric;

    ProviderQuota() {

    }

    @Override
    public String toString() {
        return quotaName+","+maxTaskCount+","+activeTaskCount+","+cpuMetric;
    }


    public static class Quota{
        public String quotaName;
        public int maxTaskCount;
        public int activeTaskCount;
        public double cpuMetric;
    }
}