package client.obj;

import protocol.Parse;
import utils.NetworkUtil;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by user on 2017/6/5.
 */
public class SerializeConnectTask implements Serializable {
    byte[] requestHostMac;//请求资源的主机地址
    private SerializeSource source;//请求得资源 ->包含资源发起者
    /**
     * 服务器udp临时端口 - 服务器完成
     */
    private byte[] serverTempUDPIp;
    private int serverTempUDPPort = 0;

    /**
     * 资源源的NET信息 - 服务器填写
     */
    private byte[] srcUDPIp;
    private int srcUDPPort;
    /**
     * 请求者的NET信息 - 服务器填写
     */
    private byte[] desUDPIp;
    private int desUDPPort;

    /**
     * 1 服务器临时端口完成
     * 3 设置了源,目的地.
     * 5 两边客户端都收到服务器的命令
     */
    private int complete = 0;


    public SerializeConnectTask( SerializeSource source) {
        this.source = source;
    }
    public void setRequestHostMac(byte[] requestHostMac){
        this.requestHostMac = requestHostMac;
    }
    public void setServerTempUDP(byte[] tempIp, int tempPort){
        this.serverTempUDPIp = tempIp;
        this.serverTempUDPPort = tempPort;
    }
    public InetSocketAddress getServerTempUDP() throws UnknownHostException {
        return new InetSocketAddress(InetAddress.getByAddress(serverTempUDPIp),serverTempUDPPort);
    }
    //源
    public void setSrcNET(InetSocketAddress address){
        if (complete<3 && srcUDPIp == null){
            this.srcUDPIp = address.getAddress().getAddress();
            this.srcUDPPort = address.getPort();
            complete++;
        }

    }
    public InetSocketAddress getSrcNET() throws UnknownHostException {
        return new InetSocketAddress(InetAddress.getByAddress(srcUDPIp),srcUDPPort);
    }
    //目标
    public void setDesNET(InetSocketAddress address){
        if (complete<3 && desUDPIp == null){
            this.desUDPIp = address.getAddress().getAddress();
            this.desUDPPort =  address.getPort();
            complete++;
        }
    }
    public InetSocketAddress getDesNet() throws UnknownHostException{
        return new InetSocketAddress(InetAddress.getByAddress(desUDPIp),desUDPPort);
    }
    public void setComplete(int i){
        complete = i;
    }
    public int getCompele(){
        return complete;
    }

    public String getSourceMac() {

        return NetworkUtil.macByte2String(requestHostMac);
    }

    public String getDestinationMac(){
        return NetworkUtil.macByte2String(getSource().getInitiatorMacAddress());
    }
    public SerializeSource getSource(){

        return source;
    }
}
