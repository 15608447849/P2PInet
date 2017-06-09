package server.obj;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/5/27.
 *
 */
public class IParameter {
    public int serverType = -1;
    /**
     * tcp通讯地址
     */
    public InetSocketAddress tcpLocalAddress;
    /**
     * udp 认证地址1
     * @param tcpLocalAddress
     */
    public InetSocketAddress udpLocalAddress1;
    /**
     * udp 认证地址2
     * @param tcpLocalAddress
     */
    public InetSocketAddress udpLocalAddress2;

    /**
     * UDP认证的辅助地址
     */
    public InetSocketAddress udpLocalAddress;

    /**
     * UDP认证的主服务器地址
     */
    public InetSocketAddress udpRemoteServerAddress;

    /**
     * 线程数设置
     */
    public int threadNunber = 10;

    public byte[] ipBytes;
    public int[] ports;

    public IParameter(InetAddress address,int tcpPort,int udpPort1,int udpPort2) {
        ipBytes = address.getAddress();
        ports = new int[]{tcpPort,udpPort1,udpPort2};
        tcpLocalAddress = new InetSocketAddress(address,tcpPort);
        udpLocalAddress1 = new InetSocketAddress(address,udpPort1);
        udpLocalAddress2 = new InetSocketAddress(address,udpPort2);
        serverType = 0; //通讯服务器
    }
    //udp类型认证的辅助服务器
    public IParameter(InetSocketAddress address,InetSocketAddress serverAddress) {
        udpLocalAddress = address;
        udpRemoteServerAddress = serverAddress;
        serverType = 1; //udp认证的辅助服务器
    }

}
