package com.fantow.test4.server;

import com.fantow.test4.constants.TCPConstants;

import java.io.InputStream;
import java.util.Scanner;

/**
 * @Description:
 * @Author chenyang270
 * @CreateDate
 * @Version: 1.0
 */
public class Server {
    public static void main(String[] args) {
        ServerProvider.start(TCPConstants.PORT_SERVER);

        try{
            System.in.read();
        }catch(Exception exception){
            exception.printStackTrace();
        }

        ServerProvider.stop();
    }
}
