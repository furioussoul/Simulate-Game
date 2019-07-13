package com.aliware.tianchi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author daofeng.xjf
 *
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance {

    Gson gson = new Gson();

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        if(CallbackListenerImpl.candidate != null){
            for (Invoker<T> invoker : invokers) {
                if(invoker.getUrl().toString().contains(CallbackListenerImpl.candidate.quotaName)){
//                    System.out.println("providers: " + gson.toJson(CallbackListenerImpl.map));
//                    System.out.println("pick: " + invoker.getUrl().toString());
                    return invoker;
                }
            }
        }
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }
}
