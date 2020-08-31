package com.fantow.test2;

import sun.plugin2.message.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.UUID;

/**
 * @Description:
 * @Author chenyang270
 * @CreateDate
 * @Version: 1.0
 */
// 被搜索的设备
public class NewUDPProvider {
    public static void main(String[] args) throws IOException {

        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        provider.start();

        // 通过键盘中断操作
        System.in.read();
        Provider.exit();
    }

    // 其实用不上多线程，不知道为什么教程要用多线程
    private static class Provider extends Thread{
        private final String sn;
        private static boolean done = false;
        private static DatagramSocket ds = null;

        public Provider(String sn){
            super();
            this.sn = sn;
        }

        @Override
        public void run(){
            System.out.println("NewUDPProvider started");

            try{
                ds = new DatagramSocket(20000);

                while(!done) {
                    final byte[] buf = new byte[512];
                    DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

                    ds.receive(receivePacket);

                    String ip = receivePacket.getAddress().getHostAddress();
                    int port = receivePacket.getPort();
                    int dataLen = receivePacket.getLength();
                    String data = new String(receivePacket.getData(), 0, dataLen);

                    System.out.println("NewUDPProvider receive from ip:" + ip + " port:" + port
                            + " data:" + data);

                    // 拿到Searcher方发送数据中的指定端口
                    int responsePort = MessageCreator.parsePort(data);
                    if (responsePort != -1) {
                        // 构建一份回送数据
                        String responseData = MessageCreator.buildWithSn(sn);
                        byte[] responseDataBytes = responseData.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(responseDataBytes,
                                responseDataBytes.length, receivePacket.getAddress(), responsePort);

                        ds.send(responsePacket);
                    }
                }
            }catch (Exception e){
//                e.printStackTrace();
            }finally {
                exit();
            }

        }

        private static void close(){
            if(ds != null){
                ds.close();
            }
        }

        static void exit(){
            done = true;
            close();
        }
    }

}
