package com.fantow.test4.client;

import com.fantow.test4.client.bean.ServerInfo;
import com.fantow.test4.clink.utils.ByteUtils;
import com.fantow.test4.constants.UDPConstants;
import com.fantow.test4.server.Server;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author chenyang270
 * @CreateDate
 * @Version: 1.0
 */
// 发送广播消息
public class ClientSearcher {
    private static final int LISTEN_PORT = UDPConstants.PORT_CLIENT_RESPONSE;

    public static ServerInfo searchServer(int timeout){
        System.out.println("UDPSearcher Started.");

        // 这个CountDownLatch用于调整线程间的顺序
        CountDownLatch receiveLatch = new CountDownLatch(1);
        Listener listener = null;
        try {
            listener = listen(receiveLatch);
            sendBroadcast();
            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        }catch(Exception ex){
            ex.printStackTrace();
        }

        System.out.println("UDPSearch finished.");
        if(listener == null){
            return null;
        }
        List<ServerInfo> devices = listener.getServerInfoList();

        if(devices.size() > 0){
            return devices.get(0);
        }
        return null;
    }


    private static Listener listen(CountDownLatch receiveLatch) throws InterruptedException {
        System.out.println("UDPSearcher start listen");
        CountDownLatch startLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, startLatch, receiveLatch);
        listener.start();
        startLatch.await();
        return listener;
    }

    private static void sendBroadcast() throws IOException {
        System.out.println("UDPSearcher send Broadcast.");
        DatagramSocket ds = new DatagramSocket();

        ByteBuffer buffer = ByteBuffer.allocate(128);
        buffer.put(UDPConstants.HEADER);
        buffer.putShort((short)1);
        buffer.putInt(LISTEN_PORT);

        DatagramPacket sendPacket = new DatagramPacket(buffer.array(),buffer.position() + 1);

        sendPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        sendPacket.setPort(UDPConstants.PORT_SERVER);

        ds.send(sendPacket);
        ds.close();

        System.out.println("UDPSearcher send Broadcast finished.");
    }

    public static class Listener extends Thread{
        private final int listenPort;
        private final CountDownLatch startDownLatch;
        private final CountDownLatch receiveDownLatch;
        private final List<ServerInfo> serverInfoList = new ArrayList<ServerInfo>();
        private final byte[] buffer = new byte[128];
        private final int minLen = UDPConstants.HEADER.length + 2 + 4;
        private boolean done = false;
        private DatagramSocket ds = null;

        private Listener(int listenPort,CountDownLatch startDownLatch,CountDownLatch receiveDownLatch){
            this.listenPort = listenPort;
            this.startDownLatch = startDownLatch;
            this.receiveDownLatch = receiveDownLatch;
        }

        @Override
        public void run(){
            super.run();

            startDownLatch.countDown();
            try{
                ds = new DatagramSocket(listenPort);
                // 构建接收实体
                DatagramPacket receivePacket = new DatagramPacket(buffer,buffer.length);

                while(!done){
                    // 接收到数据
                    ds.receive(receivePacket);

                    String ip = receivePacket.getAddress().getHostAddress();
                    byte[] data = receivePacket.getData();
                    int dataLen = receivePacket.getLength();

                    boolean isValid = dataLen >= minLen && ByteUtils.startsWith(data,UDPConstants.HEADER);

                    if(!isValid){
                        continue;
                    }

                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer,UDPConstants.HEADER.length,dataLen);
                    short cmd = byteBuffer.getShort();
                    int port = byteBuffer.getInt();

                    if(cmd != 2 || port <= 0){
                        System.out.println("接收到错误的响应");
                    }

                    String sn = new String(buffer,minLen,dataLen - minLen);
                    ServerInfo info = new ServerInfo(sn,ip,port);
                    serverInfoList.add(info);

                    receiveDownLatch.countDown();
                }



            }catch(Exception exception){
                exception.printStackTrace();
            }finally {
                close();
            }
        }


        private void close(){
            if(ds != null){
                ds.close();
                ds = null;
            }
        }

        List<ServerInfo> getServerInfoList(){
            done = true;
            close();
            return serverInfoList;
        }

    }

}
