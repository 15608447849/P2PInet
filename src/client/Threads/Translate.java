package client.Threads;

import client.obj.SerializeSource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by user on 2017/6/7.
 * 传输数据对象
 */
public class Translate {
    public static final int HOLDER_CLIENT_UP = 0;
    public static final int HOLDER_CLIENT_DOWN = 1;
    //
    private int holderType;
    //物理地址
    private byte[] mac;
    //本地address
    private InetSocketAddress localSocket;
    //服务器address
    private InetSocketAddress serverSocket;
    //对方客户端地址
    private InetSocketAddress terminalSocket;
    //通讯管道
    private DatagramChannel channel;
    private SerializeSource resource;
    //数据
    private ByteBuffer buffer;
    //传输模式
    private int mode;
    //是否可以传输
    private boolean connectSuccess;

    public Translate(int holderType) {
        this.holderType = holderType;
    }

    public int getHolderType(){
        return holderType;
    }

    public String getHolderTypeName(){
        return holderType==HOLDER_CLIENT_UP?"客户端-上传资源 # ":holderType==HOLDER_CLIENT_DOWN?"客户端-下载资源 # ":"无";
    }

    public byte[] getMac() {
        return mac;
    }

    public void setMac(byte[] mac) {
        this.mac = mac;
    }

    public InetSocketAddress getLocalSocket() {
        return localSocket;
    }

    public void setLocalSocket(InetSocketAddress localSocket) {
        this.localSocket = localSocket;
    }

    public InetSocketAddress getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(InetSocketAddress serverSocket) {
        this.serverSocket = serverSocket;
    }

    public InetSocketAddress getTerminalSocket() {
        return terminalSocket;
    }

    public void setTerminalSocket(InetSocketAddress terminalSocket) {
        this.terminalSocket = terminalSocket;
    }

    public DatagramChannel getChannel() {
        return channel;
    }

    public void setChannel(DatagramChannel channel) {
        this.channel = channel;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public SerializeSource getResource() {
        return resource;
    }

    public void setResource(SerializeSource resource) {
        this.resource = resource;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    //向指定地址发送消息
    public void sendMessageToTarget(ByteBuffer buf,InetSocketAddress address,DatagramChannel channel) throws IOException {
        if (buf!=null && buf.limit()>0 && address!=null && channel!=null){
            channel.send(buf,address);
        }
    }

    //检查Ip
    public void checkServerIp(InetSocketAddress serverAddress) {
        if (serverAddress!=null && getServerSocket()!=null){
            if (!getServerSocket().getAddress().equals(serverAddress.getAddress())){
                InetSocketAddress newServerAddress = new InetSocketAddress(serverAddress.getAddress(),getServerSocket().getPort());
                setServerSocket(newServerAddress);
            }
        }
    }

    public void start() {
        if (holderType==HOLDER_CLIENT_UP) new TClientUp(this);
        else if (holderType == HOLDER_CLIENT_DOWN) new TClientLoad(this);
    }
    public void setConnectSuccess(){
        connectSuccess = true;
    }
    public boolean isConnectSuccess() {
        return connectSuccess;
    }
}
