package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

/**
 * Created by user on 2017/5/22.
 */
public class NetworkUtil {

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
        if (mac==null || mac.length==0) return "00-00-00-00-00-00";
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








    //没有Net
    public static final int NotNat = -1;
    /**
     * 所有来自同一 个内部Tuple X的请求均被NAT转换至同一个外部Tuple Y，
     * 而不管这些请求是不是属于同一个应用或者是多个应用的。
     * 除此之外，当X-Y的转换关系建立之后，任意外部主机均可随时将Y中的地址和端口作为目标地址 和目标端口，向内部主机发送UDP报文，对外部请求的来源无任何限制
     */
    public static final int  Full_Cone_NAT = 1;
    /**
     * 所有来自同一个内部Tuple X的请求均被NAT转换至同一个外部Tuple Y,
     *只有当内部主机曾经发送过报文给外部主机（假设其IP地址为Z）后，外部主机才能以Y中的信息作为目标地址和目标端口，向内部 主机发送UDP请求报文.
     * NAT设备只向内转发（目标地址/端口转换）那些来自于当前已知的外部主机的UDP报文
     */
    public static final int  Restricted_Cone_NAT = 2;
    /**
     * 只有当内部主机曾经发送过报文给外部主机（假设其IP地址为Z且端口为P）之后，外部主机才能以Y中的信息作为目标地址和目标端 口，向内部主机发送UDP报文，同时，其请求报文的源端口必须为P
     */
    public static final int  Port_Restricted_Cone_NAT = 3;
    /**
     * 只有来自于同一个内部Tuple 、且针对同一目标Tuple的请求才被NAT转换至同一个外部Tuple，否则的话，NAT将为之分配一个新的外部Tuple
     *
     *  打个比方，当内部主机以相 同的内部Tuple对2个不同的目标Tuple发送UDP报文时，此时NAT将会为内部主机分配两个不同的外部Tuple，并且建立起两个不同的内、外部 Tuple转换关系。
     * 与此同时，只有接收到了内部主机所发送的数据包的外部主机才能向内部主机返回UDP报文，这里对外部返回报文来源的限制是与Port Restricted Cone一致的。
     */
    public static final int  Symmetric_NAT = 4;

    public static int bitwise(int a,int b){
        return (a<<2) + (a&b) + (a|b);
    }




    /**
     * 两边都在公网
     */
    public static final int MODE_NOTNAT_NOTNAT = bitwise(NotNat,NotNat);
    /**
     * A,公网 b full
     */
    public static final int MODE_NOTNAT_FULL = bitwise(NotNat,Full_Cone_NAT);//
    /**
     * A 公网 B symm
     */
    public static final int MODE_NOTNAT_SYMM = bitwise(NotNat,Symmetric_NAT);
    /**
     * 两边full
     */
    public static final int MODE_FULL_FULL = bitwise(Full_Cone_NAT,Full_Cone_NAT);
    /**
     * A-full b-symm
     */
    public static final int MODE_FULL_SYMM =  bitwise(Full_Cone_NAT,Symmetric_NAT);
    /**
     * A full,B Not
     */
    public static final int MODE_FULL_NOTNAT = bitwise(Full_Cone_NAT,NotNat);//

    /**
     * A SYMM ,B NOT
     */
    public static final int MODE_SYMM_NOTNAT = bitwise(Symmetric_NAT,NotNat);
    /**
     * a-symm b-full
     */
    public static final int MODE_SYMM_FULL = bitwise(Symmetric_NAT,Full_Cone_NAT);
    /**
     * 两边sym
     * 4 + 4 = 8
     */
    public static final int MODE_STMM_SYMM = bitwise(Symmetric_NAT,Symmetric_NAT);

}
