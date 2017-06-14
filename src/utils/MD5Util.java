package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by user on 2017/6/5.
 */
public class MD5Util {

    protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9','a', 'b', 'c', 'd', 'e', 'f' };
    protected static MessageDigest messagedigest = null;
    static{
        try{
            messagedigest = MessageDigest.getInstance("MD5");
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }

    /**
     * 获取文件md5的byte值
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] getFileMD5Bytes(File file) throws IOException {
        FileChannel ch = new RandomAccessFile(file,"rw").getChannel();
        MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());//内存映射
        messagedigest.update(byteBuffer);
        ch.close();
        new MPrivilegedAction(byteBuffer);
        return messagedigest.digest();
    }

    /**
     * 获取文件MD5的String
     * @param file
     * @return
     * @throws IOException
     */
    public static String getFileMD5String(File file) throws IOException {
        return bytesGetMD5String(getFileMD5Bytes(file));
    }

    /**
     * 获取String的MD5值
     * @param s
     * @return
     */
    public static String getMD5String(String s) {
        byte[] bytes = s.getBytes();
        messagedigest.update(bytes);
        return bytesGetMD5String(messagedigest.digest());
    }

    /**
     * 获取字节的md5->16进制字符串
     * @param bytes
     * @return
     */
    public static String bytesGetMD5String(byte[] bytes) {
        return bufferToHex(bytes);
    }

    /**
     * byte->16进制
     * @param bytes
     * @return
     */
    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    /**
     * 截取一段byte变16进制字符串
     * @param bytes
     * @param m
     * @param n
     * @return
     */
    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }


    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }



//    public static void main(String[] args) throws IOException {
//        long begin = System.currentTimeMillis();
//        File big = new File("C:\\FileServerDirs\\source\\ace.mp4");
//        String md5=getFileMD5String(big);
//        long end = System.currentTimeMillis();
//        System.out.println("MD5:"+md5+" ,TIME:"+(end-begin)+"毫秒");
//        byte[] bytes = getFileMD5Bytes(big);
//        md5 = bytesGetMD5String(bytes);
//        System.out.println("MD5:"+md5);
//    }

}
