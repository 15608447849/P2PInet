package client.Threads;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by user on 2017/6/7.
 * 传输数据对象
 */
public class Translate {

    //物理地址
    private byte[] mac;
    //本地address
    private InetSocketAddress localSokcet;
    //服务器address
    private InetSocketAddress serverSocket;
    //对方客户端地址
    private InetSocketAddress terminalSocket;
    //通讯管道
    private DatagramChannel channel;
    //数据
    private ByteBuffer buffer;


    public byte[] getMac() {
        return mac;
    }

    public void setMac(byte[] mac) {
        this.mac = mac;
    }

    public InetSocketAddress getLocalSokcet() {
        return localSokcet;
    }

    public void setLocalSokcet(InetSocketAddress localSokcet) {
        this.localSokcet = localSokcet;
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
}
