package com.aliware.tianchi;

import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.listener.CallbackListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
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
    public static Queue<String> queue;

    private static Timer timer = new Timer();

    static {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (Map.Entry<String, ProviderQuota.Quota> entry : map.entrySet()) {
                        if(System.currentTimeMillis() - entry.getValue().heartbeat >= 2000){
                            UserLoadBalance.exclude.add(entry.getKey());
                        }
                    }
                } catch (Throwable t1) {
                    t1.printStackTrace();
                }
            }
        }, 0, 1000);
    }

    @Override
    public void receiveServerMsg(String msg) {
        String[] split = msg.split(",");
        if (split.length > 0) {
            String quotaName = split[0];
            Invoker invoker = UserLoadBalance.quotaNameToInvoker.get(quotaName);
            UserLoadBalance.errorMap.put(invoker.getUrl().getPort(), new AtomicInteger(0));
            int maxTaskCount = Integer.parseInt(split[1]);
            int activeTaskCount = Integer.parseInt(split[2]);
            ProviderQuota.Quota quota = new ProviderQuota.Quota();
            quota.quotaName = quotaName;
            quota.maxTaskCount = maxTaskCount;
            quota.heartbeat = System.currentTimeMillis();
            map.put(quotaName, quota);
            //工作线程超过总线程90%时暂时禁用
            double taskCountRate = (double) activeTaskCount / (double) maxTaskCount;
            if (taskCountRate > 0.9) {
                UserLoadBalance.exclude.add(quotaName);
                return;
            }

           /* if (UserLoadBalance.exclude.contains(quotaName) && taskCountRate < 0.6 && CallbackListenerImpl.map.size() == 3) {
                synchronized (CallbackListenerImpl.class) {
                    if (UserLoadBalance.exclude.contains(quotaName) && taskCountRate < 0.6 && CallbackListenerImpl.map.size() == 3) {
                        System.out.println("re createQueue");
                        CallbackListenerImpl.createQueue();
                        UserLoadBalance.exclude.remove(quotaName);
                    }
                }
            }*/
        }

        if (map.size() == 3 && queue == null
                || queue != null && queue.size() == 0) {
            synchronized (CallbackListenerImpl.class) {
                if (map.size() == 3 && queue == null
                        || queue != null && queue.size() == 0) {
                    createQueue();
                }
            }
        }

        System.out.println("receive quota from server :" + msg);
    }

    static public void createQueue() {

        List<Map.Entry<String, ProviderQuota.Quota>> entryList = new ArrayList<>(
                map.entrySet());

        entryList.sort(Comparator.comparingInt(p -> p.getValue().maxTaskCount));

        Map.Entry<String, ProviderQuota.Quota> first = entryList.get(0);

        double[] times = new double[3];

        int total = 200;

        for (int i = 0; i < entryList.size(); i++) {
            double time = (double) entryList.get(i).getValue().maxTaskCount / (double) first.getValue().maxTaskCount;
            times[i] = time;
        }

        queue = new LinkedBlockingQueue<>(total);

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
