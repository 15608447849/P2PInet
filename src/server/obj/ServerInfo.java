package server.obj;

import server.abs.IParameter;
import utils.NetUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/5/31.
 */
public class ServerInfo extends IParameter {
    /**
     * 本地地址1
     * tcp通讯地址
     */
    public InetSocketAddress localAddress_1;
    public byte[] localMac;
    public void setLocalAddress_1(InetSocketAddress localAddress_1) {
        this.localAddress_1 = localAddress_1;
    }
    public void setLocalMac(byte[] localMac) {
        this.localMac = localMac;
    }

    @Override
    public String getMac() {
        return NetUtil.macByte2String(localMac);
    }

    @Override
    public int getPort() {
        return localAddress_1.getPort();
    }

    @Override
    public byte[] getIpBytes() {
        return localAddress_1.getAddress().getAddress();
    }
}
