package com.aliware.tianchi;

import org.apache.dubbo.remoting.exchange.Request;
import org.apache.dubbo.remoting.transport.RequestLimiter;

import java.util.Map;

/**
 * @author daofeng.xjf
 *
 * 服务端限流
 * 可选接口
 * 在提交给后端线程池之前的扩展，可以用于服务端控制拒绝请求
 */
public class TestRequestLimiter implements RequestLimiter {

    static CpuMonitor cpuMonitor;
    static {
        cpuMonitor = new CpuMonitor();
        cpuMonitor.init();
    }

    /**
     * @param request 服务请求
     * @param activeTaskCount 服务端对应线程池的活跃线程数
     * @return  false 不提交给服务端业务线程池直接返回，客户端可以在 Filter 中捕获 RpcException
     *          true 不限流
     */
    @Override
    public boolean tryAcquire(Request request, int activeTaskCount) {
        System.out.println(String.format("quota: %s, maxCount: %s, activeTaskCount: %s, cpu usage: %s",
                System.getProperty("quota"),Thread.getAllStackTraces().size(), activeTaskCount,cpuMonitor.getCPUMetric()));
        return true;
    }
}
