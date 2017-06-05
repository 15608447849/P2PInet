package client.obj;

import protocol.Parse;
import utils.NetUtil;

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
    byte[] source;//请求得资源 ->包含资源发起者
    /**
     * 服务器udp临时端口 - 服务器完成
     */
    int serverTempUDPPort = 0;

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
     * 2 源主机成功连接服务器临时端口
     * 3 目标主机成功连接临时端口
     *
     */
    private int complete = 0;
    public SerializeConnectTask( byte[] source) {
        this.source = source;
    }
    public void setRequestHostMac(byte[] requestHostMac){
        this.requestHostMac = requestHostMac;
    }
    public void setServerTempUDPPort(int tempPort){
        this.serverTempUDPPort = tempPort;
    }
    public int getServerTempUDPPort() {
        return serverTempUDPPort;
    }
    //源
    public void setSrcNET(byte[] ip,int port){
        this.srcUDPIp = ip;
        this.srcUDPPort = port;
    }
    public InetSocketAddress getSrcNET() throws UnknownHostException {
        return new InetSocketAddress(InetAddress.getByAddress(srcUDPIp),srcUDPPort);
    }
    //目标
    public void setDesNET(byte[] ip,int port){
        this.desUDPIp = ip;
        this.desUDPPort = port;
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
        return NetUtil.macByte2String(requestHostMac);
    }

    public String getDestinationMac() throws IOException, ClassNotFoundException {
        return NetUtil.macByte2String(((SerializeSource)Parse.bytes2Sobj(source)).getInitiatorMacAddress());
    }
}
