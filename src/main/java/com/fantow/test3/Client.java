package com.fantow.test3;

import java.io.*;
import java.net.*;

/**
 * @Description:
 * @Author chenyang270
 * @CreateDate
 * @Version: 1.0
 */
public class Client {
    // 连接到远程服务器的端口
    private static final int PORT = 20000;
    // 本地使用的端口
    private static final int LOCAL_PORT = 20001;

    public static void main(String[] args) throws IOException {
        Socket socket = createSocket();

        initSocket(socket);

        // 连接远程ip+port
        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(),PORT),30000);

        System.out.println("已发起服务器连接");

        try{
            todo(socket);
        }catch(Exception ex){
            System.out.println("异常关闭");
        }finally{
            socket.close();
            System.out.println("客户端已退出...");
        }
    }

    private static Socket createSocket() throws IOException {

        Socket socket = new Socket();
        socket.bind(new InetSocketAddress(Inet4Address.getLocalHost(),LOCAL_PORT));

        return socket;
    }

    private static void initSocket(Socket socket) throws SocketException {
        // 设置读取超时时间(这个设置仅仅是用于有阻塞的操作),如果超时，会引发超时异常
        socket.setSoTimeout(3000);

        // 是否复用socket
        socket.setReuseAddress(true);

        // 是否开启Nagle算法
        socket.setTcpNoDelay(false);

        // 是否在长时间无数据响应时发送确认数据(默认是2小时)
        // 其实一般的心跳机制，都是通过应用层实现
        socket.setKeepAlive(true);

        // 对于close关闭操作行为进行怎样的处理，默认为false，0
        // false,0：默认情况，关闭立即返回，会有操作系统底层接管缓冲区，将缓冲区的数据发送完成
        // true,0：关闭时立即返回，缓冲区数据将会被抛弃，直接发送RST命令给对方，并且无需等待2MSL
        // true,200：阻塞200ms后，再将缓冲区抛弃，发送RST。
        socket.setSoLinger(false,0);

        // 是否让紧急数据内敛,紧急数据通过socket.sendUrgentData(1)发送
        // 使用的是TCP中的紧急指针，一般不会用
        socket.setOOBInline(false);

        // 设置接受/发送缓冲区大小(默认是32Mb)
        socket.setSendBufferSize(64 * 1024 * 1024);
        socket.setReceiveBufferSize(64 * 1024 * 1024);
    }


    private static void todo(Socket client) throws IOException {
        // 构建键盘输入流
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        // 得到socket的输出流，并转换为打印流
        OutputStream outputStream = client.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(outputStream);

        // 得到socket输入流，并转换为BufferedReader
        InputStream inputStream = client.getInputStream();
        BufferedReader socketBufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        boolean flag = true;
        do{
//            XXX

        }while(flag);



    }


}
