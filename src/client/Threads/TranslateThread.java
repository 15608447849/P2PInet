package client.Threads;

import protocol.Command;
import protocol.Parse;
import utils.LOG;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by user on 2017/6/7.
 */
public abstract class TranslateThread extends Thread{
    protected Translate tanslate;

    public TranslateThread(Translate tanslate) {
        this.tanslate = tanslate;
        LOG.I("本机UDP地址>>"+tanslate.getLocalSokcet());
        LOG.I("服务器UDP地址>>"+tanslate.getServerSocket());
    }
    /**
     * 打开管道
     */
    protected void openChannel() throws Exception{
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(tanslate.getLocalSokcet());
        channel.configureBlocking(false);
        ByteBuffer buffer = ByteBuffer.allocate(Parse.buffSize);
        tanslate.setChannel(channel);
        tanslate.setBuffer(buffer);
    }
    /**
     * 向服务器发送消息
     */
    protected void sendMessageToServer() throws Exception{
        boolean isSend = true;
        ByteBuffer byteBuffer = tanslate.getBuffer();
        while (isSend){
            sendUdpHeartbeatToServer();//发送心跳到服务器
            byteBuffer.clear();
            InetSocketAddress socket = (InetSocketAddress) tanslate.getChannel().receive(tanslate.getBuffer());
            if (socket!=null && socket.equals(tanslate.getServerSocket())){
                //收到服务器消息
                byteBuffer.flip();
                isSend = onServerMessage(byteBuffer);
            }
            synchronized (this){
                wait(1000 * 2);
            }
        }
    }
    /**
     * 收到服务器的信息
     */
    abstract boolean onServerMessage(ByteBuffer byteBuffer);


    /**
     * 向对端发送消息
     */
    abstract void sendMessageToTerminal() throws Exception;

    /**
     * 数据传输
     */
    abstract void tanslateData() throws Exception;

    /**
     * 关闭连接
     */
    abstract void closeChannel() throws Exception;
    @Override
    public void run() {

        try {
            //打开连接
            openChannel();
            //与服务器通讯
            sendMessageToServer();
            //与对端通讯
            sendMessageToTerminal();
            //数据传输
            tanslateData();
            //关闭连接
            closeChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送心跳到服务器
     */
    public void sendUdpHeartbeatToServer() throws IOException {
            ByteBuffer buffer = tanslate.getBuffer();
            buffer.clear();
            buffer.put(Command.Client.udpHeartbeat);
            buffer.put(tanslate.getMac());
            buffer.flip();
            tanslate.sendMessageToTarget(buffer,tanslate.getServerSocket(),tanslate.getChannel());
    }



}

