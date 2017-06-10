package client.obj;

import utils.NetworkUtil;

import java.net.InetSocketAddress;
import java.net.SocketException;

import static utils.NetworkUtil.*;

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
    public int natType;

    public Info(InetSocketAddress localAddress, InetSocketAddress serverAddress) throws SocketException {
        this.localAddress = localAddress;
        this.serverAddress = serverAddress;
        this.localMac = NetworkUtil.getMACAddress(localAddress.getAddress());

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
    public String getLocalMacString(){
        return NetworkUtil.macByte2String(localMac);
    }

    public boolean isNatAuthentic() {
        return natType== NotNat || natType ==Full_Cone_NAT || natType ==  Restricted_Cone_NAT|| natType == Port_Restricted_Cone_NAT || natType== Symmetric_NAT;
    }
}
