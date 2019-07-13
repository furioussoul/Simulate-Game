package com.aliware.tianchi;

import com.google.gson.Gson;
import org.apache.dubbo.rpc.listener.CallbackListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author daofeng.xjf
 * <p>
 * 客户端监听器
 * 可选接口
 * 用户可以基于获取获取服务端的推送信息，与 CallbackService 搭配使用
 */
public class CallbackListenerImpl implements CallbackListener {

    Gson gson = new Gson();
    public static Map<String, ProviderQuota.Quota> map = new ConcurrentHashMap<>(3);
    public static ProviderQuota.Quota candidate;

    static {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {

            if (map.isEmpty()) {
                return;
            }

            List<Map.Entry<String, ProviderQuota.Quota>> entryList = new ArrayList<>(
                    map.entrySet());

            entryList.sort((p1, p2) -> {

                ProviderQuota.Quota p1v = p1.getValue();
                ProviderQuota.Quota p2v = p2.getValue();

                double remainCpu1 = 1 - p1v.cpuMetric;
                double remainCpu2 = 1 - p2v.cpuMetric;

                if (remainCpu1 >= 0.8 && remainCpu2 < 0.8) {
                    return -1;
                } else if (remainCpu2 >= 0.8 && remainCpu1 < 0.8) {
                    return 1;
                }


                double usedTaskRatio1 = (double) p1v.activeTaskCount / (double) p1v.maxTaskCount;
                double usedTaskRatio2 = (double) p1v.activeTaskCount / (double) p2v.maxTaskCount;

                if (usedTaskRatio1 <= 0.3 && usedTaskRatio2 >= 0.3) {
                    return -1;
                } else if (usedTaskRatio2 <= 0.3 && usedTaskRatio1 >= 0.3) {
                    return 1;
                }


                return (int) (remainCpu2 - usedTaskRatio2 - remainCpu1 + usedTaskRatio1);
            });


            candidate = entryList.get(0).getValue();
        }, 0, 1, TimeUnit.SECONDS);
    }


    @Override
    public void receiveServerMsg(String msg) {
        ProviderQuota.Quota providerQuota = gson.fromJson(msg, ProviderQuota.Quota.class);
        if (providerQuota != null) {
            CallbackListenerImpl.map.put(providerQuota.quotaName, providerQuota);
        }
        System.out.println("receive quota from server :" + msg);
    }
}
