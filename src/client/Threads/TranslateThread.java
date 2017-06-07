package client.Threads;

import protocol.Command;
import protocol.Parse;
import utils.LOG;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import static client.Threads.Translate.HOLDER_CLIENT_A;
import static client.Threads.Translate.HOLDER_CLIENT_B;

/**
 * Created by user on 2017/6/7.
 */
public abstract class TranslateThread extends Thread{

    protected final String TAG;
    protected Translate translate;
    public TranslateThread(Translate translate) {
        this.translate = translate;
        LOG.I("本机UDP地址>>"+translate.getLocalSokcet());
        LOG.I("服务器UDP地址>>"+translate.getServerSocket());
        TAG = translate.getHolderTypeName();
    }
    /**
     * 打开管道
     */
    protected void openChannel() throws Exception{
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(translate.getLocalSokcet());
        channel.configureBlocking(false);
        ByteBuffer buffer = ByteBuffer.allocate(Parse.buffSize);
        translate.setChannel(channel);
        translate.setBuffer(buffer);
    }
    /**
     * 向服务器发送消息
     */
    protected void sendMessageToServer() throws Exception{
        boolean isSend = true;
        ByteBuffer byteBuffer = translate.getBuffer();
        while (isSend){
            sendUdpHeartbeatToServer();//发送心跳到服务器
            byteBuffer.clear();
            InetSocketAddress socket = (InetSocketAddress) translate.getChannel().receive(translate.getBuffer());
            if (socket!=null && socket.equals(translate.getServerSocket())){
                //收到服务器消息
                byteBuffer.flip();
                isSend = onServerMessage(socket,byteBuffer);
            }
            synchronized (this){
                wait(100);
            }
        }
    }
    /**
     * 收到服务器的信息
     */
    abstract boolean onServerMessage(InetSocketAddress socketAddress,ByteBuffer byteBuffer);


    /**
     * 向对端发送消息
     */
    protected void sendMessageToTerminal() throws Exception{
        ByteBuffer buffer = translate.getBuffer();
        int type = translate.getHolderType();
        if (type != HOLDER_CLIENT_A || type != HOLDER_CLIENT_B) return;
        byte shakePackage = type == HOLDER_CLIENT_A?Command.Client.clientAshakePackage:Command.Client.clienBshakePackage;
        while (true){
            buffer.clear();
            buffer.put(shakePackage);
            buffer.flip();
            translate.sendMessageToTarget(buffer, translate.getTerminalSocket(), translate.getChannel());

            buffer.clear();
            InetSocketAddress terminal = (InetSocketAddress) translate.getChannel().receive(buffer);
            if (terminal!=null && terminal.equals(translate.getTerminalSocket())){
                buffer.flip();
                LOG.I(TAG+"收到对端信息, "+ buffer.get(0));
                break;
            }
            synchronized (this){
                wait(100);
            }
        }
    }

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
            ByteBuffer buffer = translate.getBuffer();
            buffer.clear();
            buffer.put(Command.Client.udpHeartbeat);
            buffer.put(translate.getMac());
            buffer.flip();
            translate.sendMessageToTarget(buffer, translate.getServerSocket(), translate.getChannel());
    }



}

