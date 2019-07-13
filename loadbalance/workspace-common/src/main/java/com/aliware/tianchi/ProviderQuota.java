package com.aliware.tianchi;

public enum ProviderQuota {
    INSTANCE;

    public String quotaName;
    public int maxTaskCount;
    public int activeTaskCount;
    public double cpuMetric;

    ProviderQuota() {

    }

    public static class Quota{
        public String quotaName;
        public int maxTaskCount;
        public int activeTaskCount;
        public double cpuMetric;
    }

    public Quota cloneQuota(){
        Quota quota = new Quota();
        quota.quotaName = INSTANCE.quotaName;
        quota.maxTaskCount = INSTANCE.maxTaskCount;
        quota.activeTaskCount = INSTANCE.activeTaskCount;
        quota.cpuMetric = INSTANCE.cpuMetric;
        return quota;
    }
}