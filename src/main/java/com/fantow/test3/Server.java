package com.fantow.test3;

import java.io.IOException;
import java.net.*;

/**
 * @Description:
 * @Author chenyang270
 * @CreateDate
 * @Version: 1.0
 */
public class Server {
    private static final int PORT = 20000;

    public static void main(String[] args) throws IOException {

        ServerSocket server = createServerSocket();

        initServerSocket(server);

        // 绑定端口
        // 这个backlog是指允许等待的连接队列最大容量为50
        server.bind(new InetSocketAddress(Inet4Address.getLocalHost(),PORT),50);

        System.out.println("服务器准备就绪");


        while(true){
            Socket client = server.accept();
            ClientHandler clientHandler = new ClientHandler(client);
            // 启动线程
            clientHandler.start();
        }


    }

    private static ServerSocket createServerSocket() throws IOException {
        ServerSocket server = new ServerSocket();

        return server;
    }

    private static void initServerSocket(ServerSocket socket) throws SocketException {
        // 是否复用socket
        socket.setReuseAddress(true);
        // 设置socket的接收队列
        socket.setReceiveBufferSize(64 * 1024 * 1024);
    }





    private static class ClientHandler extends Thread{

        private final Socket client;


        public ClientHandler(Socket client){
            this.client = client;
        }

        @Override
        public void run() {
            super.run();
        }
    }


}
