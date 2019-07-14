package com.aliware.tianchi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class WeightRoundRobin {

    private List<Server> servers;

    private int currentIndex;
    private int totalServer;
    private int currentWeight;
    private int maxWeight;
    private int gcdWeight;

    public WeightRoundRobin(List<Server> servers) {
        this.servers = servers;
        totalServer = servers.size();
        currentIndex = totalServer - 1;
        maxWeight = maxWeight();
        gcdWeight = serverGcd();
    }

    public Server round() {
        while (true) {
            currentIndex = (currentIndex + 1) % totalServer;
            if (currentIndex == 0) {
                currentWeight = currentWeight - gcdWeight;
                if (currentWeight <= 0) {
                    currentWeight = maxWeight;
                    if (currentWeight == 0) {
                        return null;
                    }
                }
            }

            if (servers.get(currentIndex).getWeight() >= currentWeight) {
                return servers.get(currentIndex);
            }
        }
    }

    /**
     * 返回所有服务器的权重的最大公约数
     *
     * @return
     */
    private int serverGcd() {
        int comDivisor = 0;
        for (int i = 0; i < totalServer - 1; i++) {
            if (comDivisor == 0) {
                comDivisor = gcd(servers.get(i).getWeight(), servers.get(i + 1).getWeight());
            } else {
                comDivisor = gcd(comDivisor, servers.get(i + 1).getWeight());
            }
        }
        return comDivisor;
    }

    /**
     * 获得服务器中的最大权重
     *
     * @return
     */
    private int maxWeight() {
        int max = servers.get(0).getWeight();
        int tmp;
        for (int i = 1; i < totalServer; i++) {
            tmp = servers.get(i).getWeight();
            if (max < tmp) {
                max = tmp;
            }
        }
        return max;
    }

    /**
     * 求两个数的最大公约数 4和6最大公约数是2
     *
     * @param num1
     * @param num2
     * @return
     */
    private int gcd(int num1, int num2) {
        BigInteger i1 = new BigInteger(String.valueOf(num1));
        BigInteger i2 = new BigInteger(String.valueOf(num2));
        return i1.gcd(i2).intValue();
    }


    private static int concurrentSize = 100;

    public static void main(String[] args) throws InterruptedException {

        List<Server> servers = new ArrayList<>();
        servers.add(new Server("small", 2));
        servers.add(new Server("medium", 4));
        servers.add(new Server("large", 6));

        WeightRoundRobin wr = new WeightRoundRobin(servers);

        Map<String, AtomicInteger> map = new ConcurrentHashMap<>(3);

        final CyclicBarrier cb = new CyclicBarrier(concurrentSize);
        for (int i = 0; i < concurrentSize; i++) {
            new Thread(() -> {
                try {
                    cb.await();
                    System.out.println(Thread.currentThread().getName() + " " + wr.round());
                    Server server = wr.round();
                    AtomicInteger atomicInteger = map.putIfAbsent(server.ip, new AtomicInteger());
                    if(atomicInteger != null){
                        atomicInteger.incrementAndGet();
                    }
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }, "thread" + i).start();
        }

        Thread.sleep(1000);

        for (Map.Entry<String, AtomicInteger> entry : map.entrySet()) {
            System.out.println(String.format("server: %s, count: %s", entry.getKey(),entry.getValue()));
        }
    }


    public static class Server {
        private String ip;
        private int weight;

        public Server(String ip) {
            super();
            this.ip = ip;
        }

        public Server(String ip, int weight) {
            this.ip     = ip;
            this.weight = weight;
        }

        public String getIp() {
            return ip;
        }
        public void setIp(String ip) {
            this.ip = ip;
        }
        public int getWeight() {
            return weight;
        }
        public void setWeight(int weight) {
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "Server [ip=" + ip + ", weight=" + weight + "]";
        }
        }

}
