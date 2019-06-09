package com.chen.common.utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * @author : goldgreat
 * @Description :
 * @Date :  2019/5/24 9:57
 */
public class FileUtil {
    public static void writeFile(String filePath,String content) throws  Exception{
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath,true)));
        out.write(content+"\r\n");
        out.close();
    }

}
