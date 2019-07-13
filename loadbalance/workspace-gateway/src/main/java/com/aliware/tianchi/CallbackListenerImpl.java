package com.aliware.tianchi;

import com.google.gson.Gson;
import org.apache.dubbo.rpc.listener.CallbackListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author daofeng.xjf
 *
 * 客户端监听器
 * 可选接口
 * 用户可以基于获取获取服务端的推送信息，与 CallbackService 搭配使用
 *
 */
public class CallbackListenerImpl implements CallbackListener {

    Gson gson = new Gson();
    public static Map<String,ProviderQuota.Quota> map = new ConcurrentHashMap<>(3);
    public static ProviderQuota.Quota candidate;

    static {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {

            if(map.isEmpty()){
                return;
            }

            List<Map.Entry<String, ProviderQuota.Quota>> entryList = new ArrayList<>(
                    map.entrySet());

            entryList.sort((p1, p2) -> {

                ProviderQuota.Quota p1v = p1.getValue();
                ProviderQuota.Quota p2v = p2.getValue();

                double remainCpu1 = 1 - p1v.cpuMetric;
                double remainCpu2 = 1 - p2v.cpuMetric;

                double diff = remainCpu1 - remainCpu2;

                if(diff > 0.1){
                    return -1;
                }else if(diff < -0.1){
                    return 1;
                }

                int remainTaskCount1 = p1v.maxTaskCount - 10 - p1v.activeTaskCount;
                int remainTaskCount2 = p2v.maxTaskCount - 10 - p2v.activeTaskCount;

                return (int) (remainCpu2 * 100 + remainTaskCount2 - remainCpu1 * 100 - remainTaskCount1);
            });


            candidate = entryList.get(0).getValue();
        },0,200, TimeUnit.MILLISECONDS);
    }


    @Override
    public void receiveServerMsg(String msg) {
        ProviderQuota.Quota providerQuota = gson.fromJson(msg, ProviderQuota.Quota.class);
        if(providerQuota != null){
            CallbackListenerImpl.map.put(providerQuota.quotaName,providerQuota);
        }
        System.out.println("receive quota from server :" + msg);
    }
}
