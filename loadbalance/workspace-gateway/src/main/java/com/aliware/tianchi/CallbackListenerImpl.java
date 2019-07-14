package com.aliware.tianchi;

import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.listener.CallbackListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author daofeng.xjf
 * <p>
 * 客户端监听器
 * 可选接口
 * 用户可以基于获取获取服务端的推送信息，与 CallbackService 搭配使用
 */
public class CallbackListenerImpl implements CallbackListener {

    public static Map<String, ProviderQuota.Quota> map = new ConcurrentHashMap<>(3);

    @Override
    public void receiveServerMsg(String msg) {
        String[] split = msg.split(",");
        if (map.size() < 3 && split.length > 0) {
            String quotaName = split[0];
            int maxTaskCount = Integer.parseInt(split[1]);
            ProviderQuota.Quota quota = new ProviderQuota.Quota();
            quota.quotaName = quotaName;
            quota.maxTaskCount = maxTaskCount;
            map.put(quotaName, quota);
        }

        System.out.println("receive quota from server :" + msg);
    }
}
