package com.aliware.tianchi;

import com.google.gson.Gson;
import org.apache.dubbo.rpc.listener.CallbackListener;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author daofeng.xjf
 * <p>
 * 客户端监听器
 * 可选接口
 * 用户可以基于获取获取服务端的推送信息，与 CallbackService 搭配使用
 */
public class CallbackListenerImpl implements CallbackListener {

    public static Map<String, ProviderQuota.Quota> map = new ConcurrentHashMap<>(3);
    public static Queue<String> queue;

    @Override
    public void receiveServerMsg(String msg) {
        String[] split = msg.split(",");
        if (split.length > 0) {
            String quotaName = split[0];
            int maxTaskCount = Integer.parseInt(split[1]);
            ProviderQuota.Quota quota = new ProviderQuota.Quota();
            quota.quotaName = quotaName;
            quota.maxTaskCount = maxTaskCount;
            map.put(quotaName, quota);
        }

        if (map.size() == 3 && queue == null) {
            createQueue();
        }

        System.out.println("receive quota from server :" + msg);
    }

    private void createQueue() {

        List<Map.Entry<String, ProviderQuota.Quota>> entryList = new ArrayList<>(
                map.entrySet());

        entryList.sort(Comparator.comparingInt(p -> p.getValue().maxTaskCount));

        Map.Entry<String, ProviderQuota.Quota> first = entryList.get(0);

        double[] times = new double[3];

        int total = 0;

        for (int i = 0; i < entryList.size(); i++) {
            double time = (double) entryList.get(i).getValue().maxTaskCount / (double) first.getValue().maxTaskCount;
            times[i] = time;
            total += entryList.get(i).getValue().maxTaskCount;
        }

        queue=new LinkedBlockingQueue<>(total);

        int count = 0;

        do {
            for (int i = 0; i < times.length; i++) {
                for (int time = (int) times[i]; time > 0; time--) {
                    queue.offer(entryList.get(i).getValue().quotaName);
                    count++;
                }
            }
        } while (count < total);
    }
}
