package com.fantow.test2;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Description:
 * @Author chenyang270
 * @CreateDate
 * @Version: 1.0
 */
public class NewUDPSearcher {

    // Searcher方的监听端口号为30000
    private static final int LISTENER_PORT = 30000;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("UDPSearcher Started.");

        Listener listeners = listen();
        sendBoardCast();


        List<Device> deviceList = listeners.getDevices();
        for(Device device : deviceList){
            System.out.println(device);
        }

        System.out.println("UDPSearcher Finished.");
    }

    private static Listener listen() throws InterruptedException {
        System.out.println("UDPSearcher start listener.");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTENER_PORT,countDownLatch);
        listener.start();

        countDownLatch.await();
        return listener;
    }

    // 发送广播的实现
    private static void sendBoardCast() throws IOException {
        String sendMsg = MessageCreator.buildWithPort(LISTENER_PORT);

        DatagramSocket ds = new DatagramSocket();

        byte[] sendMsgBytes = sendMsg.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendMsgBytes,sendMsgBytes.length);

        // 这个数据包要发送到的ip和端口
        sendPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        sendPacket.setPort(20000);

        ds.send(sendPacket);
        ds.close();

        System.out.println("UDPSearcher sendBroadcast finished.");
    }

    private static class Device{
        final String ip;
        final int port;
        final String sn;

        private Device(String ip,int port,String sn){
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Device{");
            sb.append("ip='").append(ip).append('\'');
            sb.append(", port=").append(port);
            sb.append(", sn='").append(sn).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }


    private static class Listener extends Thread{
        private final int listenPort;
        private final CountDownLatch countDownLatch;
        private final List<Device> devices = new ArrayList<Device>();
        private boolean done = false;
        private static DatagramSocket ds = null;

        public Listener(int listenPort,CountDownLatch countDownLatch){
            super();
            this.listenPort = listenPort;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            super.run();

            countDownLatch.countDown();
            try{
                ds = new DatagramSocket(listenPort);
                while(!done){
                    // 构建接收实体
                    final byte[] buf = new byte[512];
                    DatagramPacket receivePacket = new DatagramPacket(buf,buf.length);

                    ds.receive(receivePacket);

                    // 打印接收到的信息
                    String ip = receivePacket.getAddress().getHostAddress();
                    int port = receivePacket.getPort();
                    int dataLen = receivePacket.getLength();
                    String data = new String(receivePacket.getData(),0,dataLen);
                    System.out.println("UDPSearcher receive from ip:" + ip + " port:" + port
                                        + " data:" +data);

                    String sn = MessageCreator.parseSn(data);
                    if(sn != null){
                        Device device = new Device(ip,port,sn);
                        devices.add(device);
                    }
                }
            }catch(Exception ex){

            }finally {
                exit();
            }
        }

        static void close(){
            if(ds != null){
                ds.close();
            }
        }


        void exit(){
            done = true;
            close();
        }

        public List<Device> getDevices() {
            return devices;
        }
    }
}
