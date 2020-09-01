package com.fantow.test4.clink.utils;

/**
 * @Description:
 * @Author chenyang270
 * @CreateDate
 * @Version: 1.0
 */
public class ByteUtils {
    public static boolean startsWith(byte[] source,byte[] match){
        return startsWith(source,0,match);
    }

    public static boolean startsWith(byte[] source,int offset,byte[] match){
        if(match.length > source.length - offset){
            return false;
        }

        for(int i = 0;i < match.length;i++){
            if(match[i] != source[offset + i]){
                return false;
            }
        }
        return true;
    }

}
