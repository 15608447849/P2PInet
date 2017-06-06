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
}
