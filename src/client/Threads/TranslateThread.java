package client.Threads;

import client.obj.SerializeTranslate;
import protocol.Command;
import protocol.Parse;
import utils.LOG;
import utils.NetworkUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;


/**
 * Created by user on 2017/6/7.
 *
 */
public abstract class TranslateThread extends Thread{

    protected final String TAG;
    protected Translate translate;
    protected static final int OVER_TIME_INIT = 0;
    protected static final int OVER_TIME_OVER = 100;
    protected  static final int loopTime = 100;
    protected int overTimeCount = OVER_TIME_INIT;
    public TranslateThread(Translate translate) {
        this.translate = translate;
        LOG.I("本机UDP地址>>"+translate.getLocalSocket());
        LOG.I("服务器UDP地址>>"+translate.getServerSocket());
        TAG = translate.getHolderTypeName();
    }
    /**
     * 打开管道
     */
    protected void openChannel() throws Exception{
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(translate.getLocalSocket());
        channel.configureBlocking(false);
        ByteBuffer buffer = ByteBuffer.allocate(Parse.buffSize);
        translate.setChannel(channel);
        translate.setBuffer(buffer);
    }
    /**
     * 向服务器发送消息
     */
    protected void sendMessageToServer() throws Exception{

        ByteBuffer byteBuffer = translate.getBuffer();
        while (isTimeOver()){
            sendUdpHeartbeatToServer();//发送心跳到服务器
            byteBuffer.clear();
            InetSocketAddress socket = (InetSocketAddress) translate.getChannel().receive(byteBuffer);
            if (socket!=null){
                //收到服务器消息
                byteBuffer.flip();
                byte command = byteBuffer.get(0);
                if (command == Command.UDPTranslate.serverHeartbeatResp){
                    //设置对端的信息, 设置传输模式, 进入下一步
                    byte[] data = new byte[byteBuffer.limit()-1];
                    byteBuffer.position(1);
                    byteBuffer.get(data);
                    SerializeTranslate strans = (SerializeTranslate) Parse.bytes2Sobj(data);
                    String macStr = NetworkUtil.macByte2String(translate.getMac());
                    if (macStr.equals(strans.connectTask.getDownloadHostMac())){
                        translate.setTerminalSocket(strans.connectTask.getUploadHostAddress());
                    }else if (macStr.equals(strans.connectTask.getUploadHostMac())){
                        translate.setTerminalSocket(strans.connectTask.getDownloadHostAddress());
                    }
                     if (translate.getTerminalSocket()!=null){
                         translate.setMode(strans.mode);
                        //回复服务器
                         respondServerHeartbeat();
                         //进入下一步
                         overTimeCount = OVER_TIME_OVER;
                     }
                }
            }
            synchronized (this){
                this.wait(loopTime);
            }
            overTimeCount++;
        }
    }




    /**
     * 向对端发送消息
     */
    protected void sendMessageToTerminal() throws Exception{
        overTimeCount = OVER_TIME_INIT;
        int mode = translate.getMode();
        LOG.I("模式: "+ mode);
        if (mode==0) return;
        if (mode == 1){
            //主动模式
            activeMode();
        }
        if (mode == 2){
            //被动模式
            passiveMode();
        }
        if (mode == 3){
//            中转模式
            translationMode();
        }
    }

    //主动联系对方
    protected void activeMode(){
        LOG.I("进入主动连接对方 >> "+ translate.getTerminalSocket());

        ByteBuffer buffer = translate.getBuffer();
        buffer.clear();
        buffer.put(Command.UDPTranslate.shakePackage);
        buffer.flip();
        try {
            translate.sendMessageToTarget(buffer, translate.getTerminalSocket(), translate.getChannel());//发送信息
            LOG.I("握手包已发送.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (isTimeOver()){
            try {
                buffer.clear();
                InetSocketAddress terminal = (InetSocketAddress) translate.getChannel().receive(buffer);//等待接收
                LOG.I(terminal+" # " + overTimeCount);
                if (terminal!=null){
                    buffer.flip();
                    if (buffer.get(0) == Command.UDPTranslate.shakePackage_resp){
                        LOG.I(TAG+" 收到对端信息, " +terminal +" ,握手成功.进入数据传输. ");
                        translate.setConnectSuccess();
                        overTimeCount = OVER_TIME_OVER;
                    }
                }
                synchronized (this){
                    this.wait(loopTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                overTimeCount++;
            }

        }
    }
    //被动接受对方信息
    protected void passiveMode(){
        LOG.I("等待对方连接 ,当前已知地址 "+ translate.getTerminalSocket());
        ByteBuffer buffer = translate.getBuffer();
        while (isTimeOver()){
            try {
                buffer.clear();
                InetSocketAddress terminal = (InetSocketAddress) translate.getChannel().receive(buffer);//等待接收
                LOG.I(terminal+" # "  + overTimeCount );
                if (terminal!=null && terminal.getAddress().equals(translate.getTerminalSocket().getAddress())){
                    buffer.flip();
                    if (buffer.get(0) == Command.UDPTranslate.shakePackage){
                        translate.setTerminalSocket(terminal);
                        buffer.clear();
                        buffer.put(Command.UDPTranslate.shakePackage_resp);
                        buffer.flip();
                        translate.sendMessageToTarget(buffer, terminal, translate.getChannel());//发送信息
                        synchronized (this){
                            this.wait(100);
                        }
                        buffer.rewind();
                        translate.sendMessageToTarget(buffer, terminal, translate.getChannel());//发送信息
                        synchronized (this){
                            this.wait(100);
                        }
                        buffer.rewind();
                        translate.sendMessageToTarget(buffer, terminal, translate.getChannel());//发送信息
                        synchronized (this){
                            this.wait(100);
                        }
                        buffer.rewind();
                        translate.sendMessageToTarget(buffer, terminal, translate.getChannel());//发送信息
                        synchronized (this){
                            this.wait(100);
                        }
                        buffer.rewind();
                        translate.sendMessageToTarget(buffer, terminal, translate.getChannel());//发送信息
                        LOG.I(TAG+"收到对端信息, " +terminal +" ,已发送握手回执.进入数据传输. "+translate.getTerminalSocket() +" - "+buffer);
                        translate.setConnectSuccess();
                        overTimeCount = OVER_TIME_OVER;
                    }
                }
                synchronized (this){
                    this.wait(loopTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                overTimeCount++;
            }
        }
    }
    //通过服务器中转消息
    protected void translationMode(){

    }


    /**
     * 数据传输
     */
    abstract void translateData() throws Exception;

    /**
     * 关闭连接
     */
    protected void closeChannel(){
        //关闭管道
        if (translate.getChannel().isOpen()){
            try {
                translate.getChannel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LOG.I("关闭连接: " + this);
    }
    @Override
    public void run() {
        try {
            //打开连接
            openChannel();
            //与服务器通讯
            sendMessageToServer();
            //与对端通讯
            sendMessageToTerminal();
            if (translate.isConnectSuccess()){
                //数据传输
                translateData();
            }
            //关闭连接
            closeChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected boolean isTimeOver(){
        return overTimeCount<OVER_TIME_OVER;
    }
    /**
     * 发送心跳到服务器
     */
    public void sendUdpHeartbeatToServer() throws IOException {
            ByteBuffer buffer = translate.getBuffer();
            buffer.clear();
            buffer.put(Command.UDPTranslate.udpHeartbeat);
            buffer.put(translate.getMac());
            buffer.flip();
            translate.sendMessageToTarget(buffer, translate.getServerSocket(), translate.getChannel());
    }
    private void respondServerHeartbeat() throws IOException{
        //回复服务器
        ByteBuffer buffer = translate.getBuffer();
        buffer.clear();
        buffer.put(Command.UDPTranslate.serverHeartbeatResp);
        buffer.put(translate.getMac());
        buffer.flip();
        translate.sendMessageToTarget(buffer, translate.getServerSocket(), translate.getChannel());
        LOG.I("已回复客户端命令回执."+translate.getServerSocket()+" --> "+buffer);
    }


}

