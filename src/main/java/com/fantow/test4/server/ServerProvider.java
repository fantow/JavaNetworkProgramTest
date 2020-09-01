package com.fantow.test4.server;

import com.fantow.test4.clink.utils.ByteUtils;
import com.fantow.test4.constants.UDPConstants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @Description:
 * @Author chenyang270
 * @CreateDate
 * @Version: 1.0
 */
// 服务器端接收UDP消息，再将其TCP端口回送
public class ServerProvider {
    private static Provider PROVIDER_INSTANCE;

    static void start(int port){
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn,port);
        provider.start();
        PROVIDER_INSTANCE = provider;
    }

    static void stop(){
        if(PROVIDER_INSTANCE != null){
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }


    private static class Provider extends Thread{
        private final byte[] sn;
        private final int port;
        private boolean done = false;
        private DatagramSocket ds = null;

        final byte[] buffer = new byte[128];


        Provider(String sn,int port){
            super();
            this.sn = sn.getBytes();
            this.port = port;
        }

        @Override
        public void run(){
            super.run();

            System.out.println("UDPProvider Started.");

            try{
                ds = new DatagramSocket(UDPConstants.PORT_SERVER);
                DatagramPacket receivePacket = new DatagramPacket(buffer,buffer.length);

                while(!done){
                    ds.receive(receivePacket);

                    //获取发送者的IP地址
                    String clientIp = receivePacket.getAddress().getHostAddress();
                    int clientDataLen = receivePacket.getLength();
                    byte[] clientData = receivePacket.getData();
                    // 传输数据中有2位是cmd，4位是port
                    boolean isValid = clientDataLen >= (UDPConstants.HEADER.length + 2 + 4) && ByteUtils.startsWith(clientData,UDPConstants.HEADER);

                    if(!isValid){
                        continue;
                    }

                    // 解析 (命令与回送端口)
                    int index = UDPConstants.HEADER.length;
                    short cmd = (short)((clientData[index++] << 8) | (clientData[index++] & 0xff));
                    int responsePort = (((clientData[index++]) << 24 | (clientData[index++] & 0xff) << 16 |
                            (clientData[index++] & 0xff) << 8) | (clientData[index++] & 0xff));

                    // 判断数据合法性
                    if(cmd == 1 && responsePort > 0){
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short)2);
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);
                        int length = byteBuffer.position();
                        DatagramPacket responsePacket = new DatagramPacket(buffer,length,
                                receivePacket.getAddress(),responsePort);

                        ds.send(responsePacket);
                    }else{
                        System.out.println("接收到错误格式的消息.");
                    }
                }
            }catch(Exception ex){

            }finally{
                close();
            }
        }

        private void close(){
            if(ds != null){
                ds.close();
                ds = null;
            }
        }

        void exit(){
            done = true;
            close();
        }

    }

}
