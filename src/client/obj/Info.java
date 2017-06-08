package client.obj;

import utils.LOG;
import utils.NetUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created by user on 2017/6/2.
 * 用于创建客户端
 */
public class Info {

    /**
     * 本地地址
     */
    private InetSocketAddress localAddress;
    /**
     * 服务器地址
     */
    private InetSocketAddress serverAddress;
    /**
     * 本地mac地址
     */
    private byte[] localMac;

    /**
     *
     * 客户端NET 类型
     *
     * */
    private int netType = -1;

    public Info(InetSocketAddress localAddress, InetSocketAddress serverAddress) throws SocketException {
        this.localAddress = localAddress;
        this.serverAddress = serverAddress;
        this.localMac = NetUtil.getMACAddress(localAddress.getAddress());

    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    public byte[] getLocalMac() {
        return localMac;
    }

    public void setNetType(int type){
        this.netType = type;
    }
    public int getNetType(){
        return netType;
    }
    //没有Net
    public static final int No_Net = 10;
    /**
     * 所有来自同一 个内部Tuple X的请求均被NAT转换至同一个外部Tuple Y，
     * 而不管这些请求是不是属于同一个应用或者是多个应用的。
     * 除此之外，当X-Y的转换关系建立之后，任意外部主机均可随时将Y中的地址和端口作为目标地址 和目标端口，向内部主机发送UDP报文，对外部请求的来源无任何限制
     */
    public static final int  Full_Cone_NAT = 0;
    /**
     * 所有来自同一个内部Tuple X的请求均被NAT转换至同一个外部Tuple Y,
     *只有当内部主机曾经发送过报文给外部主机（假设其IP地址为Z）后，外部主机才能以Y中的信息作为目标地址和目标端口，向内部 主机发送UDP请求报文.
     * NAT设备只向内转发（目标地址/端口转换）那些来自于当前已知的外部主机的UDP报文
     */
    public static final int  Restricted_Cone_NAT = 1;
    /**
     * 只有当内部主机曾经发送过报文给外部主机（假设其IP地址为Z且端口为P）之后，外部主机才能以Y中的信息作为目标地址和目标端 口，向内部主机发送UDP报文，同时，其请求报文的源端口必须为P
     */
    public static final int  Port_Restricted_Cone_NAT = 2;
    /**
     * 只有来自于同一个内部Tuple 、且针对同一目标Tuple的请求才被NAT转换至同一个外部Tuple，否则的话，NAT将为之分配一个新的外部Tuple
     *
     *  打个比方，当内部主机以相 同的内部Tuple对2个不同的目标Tuple发送UDP报文时，此时NAT将会为内部主机分配两个不同的外部Tuple，并且建立起两个不同的内、外部 Tuple转换关系。
     * 与此同时，只有接收到了内部主机所发送的数据包的外部主机才能向内部主机返回UDP报文，这里对外部返回报文来源的限制是与Port Restricted Cone一致的。
     */
    public static final int  Symmetric_NAT = 3;
    public static final int Restricted_Cone_NAT_or_Port_Restricted_Cone_NAT = 5;
}
