package server.obj;

import utils.NetUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/5/27.
 *
 */
public class IParameter {
    /**
     * tcp通讯地址
     */
    public InetSocketAddress tcpLocalAddress;
    /**
     * udp 认证地址
     * @param tcpLocalAddress
     */
    public InetSocketAddress udpLocalAddress1;
    /**
     * udp 认证地址2
     * @param tcpLocalAddress
     */
    public InetSocketAddress udpLocalAddress2;

    private byte[] ipBytes;

    private int[] ports;
    public IParameter(InetAddress address,int tcpPort,int udpPort1,int udpPort2) {
        ipBytes = address.getAddress();
        ports = new int[]{tcpPort,udpPort1,udpPort2};
        tcpLocalAddress = new InetSocketAddress(address,tcpPort);
        udpLocalAddress1 = new InetSocketAddress(address,udpPort1);
        udpLocalAddress2 = new InetSocketAddress(address,udpPort2);
    }

    public int[] getPorts() {
        return ports;
    }

    public byte[] getIpBytes() {
        return ipBytes;
    }
}
