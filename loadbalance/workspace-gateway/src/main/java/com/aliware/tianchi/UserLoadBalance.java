package com.aliware.tianchi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
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
    public static volatile WeightRoundRobin wr = null;
    private AtomicBoolean ab = new AtomicBoolean(false);

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {

        if (!ab.get()) {
            if (CallbackListenerImpl.map.size() == 3 && ab.compareAndSet(false, true)) {
                List<WeightRoundRobin.Server> servers = new ArrayList<>(3);
                for (int i = 0; i < invokers.size(); ++i) {
                    String quotaName = invokers.get(i).getUrl().getAddress().split(":")[0].split("-")[1];
                    quotaNameToInvoker.putIfAbsent(quotaName, invokers.get(i));
                    ProviderQuota.Quota quota = CallbackListenerImpl.map.get(quotaName);
                    WeightRoundRobin.Server server = new WeightRoundRobin.Server(quotaName,
                            quotaName.equals("large") ? quota.maxTaskCount * 3: quota.maxTaskCount);
                    servers.add(server);
                }
                wr = new WeightRoundRobin(servers);
            }
        }

        if(wr == null){
            return null;
        }
        String ip = wr.round().getIp();
        System.out.println("weightRoundRobin: " + ip);
        Invoker invoker = quotaNameToInvoker.get(ip);
        return invoker;
    }
}
