package com.aliware.tianchi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author daofeng.xjf
 * <p>
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance {

    public final static Map<String, Invoker> quotaNameToInvoker = new HashMap<>(3);
    public final static Map<Integer,String> portToQuotaName = new HashMap<>(3);
    private AtomicBoolean ab = new AtomicBoolean(false);

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {

        if (!ab.get()) {
            if (ab.compareAndSet(false, true)) {
                for (int i = 0; i < invokers.size(); ++i) {
                    String quotaName = invokers.get(i).getUrl().getAddress().split(":")[0].split("-")[1];
                    quotaNameToInvoker.putIfAbsent(quotaName, invokers.get(i));
                    portToQuotaName.putIfAbsent(invokers.get(i).getUrl().getPort(),quotaName);
                }
            }
        }

        if (CallbackListenerImpl.queue != null) {
            String quotaName = CallbackListenerImpl.queue.poll();
            if (null != quotaName) {
//                System.out.println(quotaName);
                return quotaNameToInvoker.get(quotaName);
            }
        }

        Invoker invoker = invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
//        System.out.println(invoker.getUrl());
        return invoker;
    }
}
