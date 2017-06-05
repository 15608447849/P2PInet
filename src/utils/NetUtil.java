package utils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by user on 2017/5/22.
 */
public class NetUtil {

    /**
     * 得到本地IP
     * @return
     */
    public static InetAddress getLocalIPInet() {
        InetAddress ip = null;
        try {
            Enumeration<?> e1 = NetworkInterface.getNetworkInterfaces();
            while (e1.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) e1.nextElement();
                if (ni.isUp() && !ni.isLoopback()) {
                    Enumeration<?> e2 = ni.getInetAddresses();
                    while (e2.hasMoreElements()) {
                        InetAddress ia = (InetAddress) e2.nextElement();
                        if (ia instanceof Inet6Address){continue;}
                        ip = ia;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }
    /**
     * 得到本地IPString
     * @return
     */
    public static String getLocalIP() {
        return getLocalIPInet().getHostAddress();
    }


    //获取MAC地址的方法
    public static String getMACAddressToString(InetAddress ia) throws SocketException {
        //获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
        return macByte2String(getMACAddress(ia));
    }

    //获取MAC地址的方法
    public static byte[] getMACAddress(InetAddress ia) throws SocketException {
        //获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
        return NetworkInterface.getByInetAddress(ia).getHardwareAddress();
    }

    public static String macByte2String(byte[] mac){
        if (mac==null || mac.length==0) return null;
        //下面代码是把mac地址拼装成String
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<mac.length;i++){
            if(i!=0){
                sb.append("-");
            }
            //mac[i] & 0xFF 是为了把byte转化为正整数
            String s = Integer.toHexString(mac[i] & 0xFF);
            sb.append(s.length()==1?0+s:s);
        }
        //把字符串所有小写字母改为大写成为正规的mac地址并返回
        return sb.toString().toUpperCase();
    }








}
