package client.threads;

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

    private final int MAX_OVER_TIME = 30*1000;
    private long curTime = System.currentTimeMillis();

    protected void resetTime(){
        curTime = System.currentTimeMillis();
    }
    protected boolean isNotTimeout(){
        return (System.currentTimeMillis() - curTime)<=MAX_OVER_TIME;
    }

    protected void waitTime(int time){
        synchronized (this){
            try {
                this.wait(time);
            } catch (InterruptedException e) {
            }
        }
    }

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
        ByteBuffer buffer = ByteBuffer.allocate(Parse.UDP_DATA_MIN_BUFFER_ZONE);
        translate.setChannel(channel);
        translate.setBuffer(buffer);
    }
    /**
     * 向服务器发送消息
     */
    protected void sendMessageToServer() throws Exception{
        resetTime();
        ByteBuffer byteBuffer = translate.getBuffer();
        InetSocketAddress socket;
        byte command;
        while (isNotTimeout()){
            sendUdpHeartbeatToServer();//发送心跳到服务器
            byteBuffer.clear();
            socket = (InetSocketAddress) translate.getChannel().receive(byteBuffer);
            if (socket!=null){
                //收到服务器消息
                byteBuffer.flip();
                command = byteBuffer.get(0);
                if (command == Command.UDPTranslate.serverHeartbeatResp){
                    //设置对端的信息, 设置传输模式, 进入下一步
                    byte[] data = new byte[byteBuffer.limit()-1];
                    byteBuffer.position(1);
                    byteBuffer.get(data);
                    SerializeTranslate serializeTranslate = (SerializeTranslate) Parse.bytes2Sobj(data);
                    if (serializeTranslate!=null){
                        translate.setMode(serializeTranslate.mode);
                        translate.setTerminalSocket(serializeTranslate.address);
                        //回复服务器->进入下一步
                        respondServerHeartbeat();
                        break;
                    }
                }
            }

        }
    }




    /**
     * 向对端发送消息
     */
    protected void sendMessageToTerminal() throws Exception{
        int mode = translate.getMode();
        LOG.I("模式代码 : "+ mode);
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
        LOG.I("进入主动模式, 尝试连接对方 >> "+ translate.getTerminalSocket());
        resetTime();
        ByteBuffer buffer = translate.getBuffer();
        InetSocketAddress terminal;
        int count = 1;
        while (isNotTimeout()){
            try {
                buffer.clear();
                buffer.put(Command.UDPTranslate.shakePackage);
                buffer.flip();
                translate.sendMessageToTarget(buffer, translate.getTerminalSocket(), translate.getChannel());//发送信息
                LOG.I("握手包已发送 -> "+ translate.getTerminalSocket());

                buffer.clear();
                terminal = (InetSocketAddress) translate.getChannel().receive(buffer);//等待接收
                LOG.I(terminal +" =====>> " + overTimeCount);
                if (terminal!=null){
                    buffer.flip();
                    if (buffer.get(0) == Command.UDPTranslate.shakePackage_resp){
                        LOG.I(TAG+" 收到对端信息,[ " +terminal +" ],握手成功.当前尝试次数:"+ count);
                        if (count == 3){
                            LOG.I("进入数据交互.");
                            translate.setConnectSuccess();
                            break;
                        }
                        count++;
                    }
                }

                waitTime(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //被动接受对方信息
    protected void passiveMode(){
        LOG.I("进入被动模式,尝试等待对方连接 ,已知地址 [ "+ translate.getTerminalSocket()+" ]");
        ByteBuffer buffer = translate.getBuffer();
        InetSocketAddress terminal ;
        int count=1;
        while (isNotTimeout()){
            try {
                buffer.clear();
                terminal = (InetSocketAddress) translate.getChannel().receive(buffer);//等待接收
                LOG.I(terminal+" ---------> "  + overTimeCount );
                if (terminal!=null){
                    buffer.flip();
                    if (buffer.get(0) == Command.UDPTranslate.shakePackage){
                        if (count==3){
                            LOG.E("对方地址是否改变: "+ terminal.equals(translate.getTerminalSocket()));
                            translate.setTerminalSocket(terminal);
                            translate.setConnectSuccess();
                            LOG.I(".进入数据传输. "+translate.getTerminalSocket() +" - ");
                            break;
                        }


                        buffer.clear();
                        buffer.put(Command.UDPTranslate.shakePackage_resp);
                        buffer.flip();

                        translate.sendMessageToTarget(buffer, terminal, translate.getChannel());//发送信息
                        LOG.I(TAG+"收到对端信息- " + terminal +"已发送握手回执.");
                        count++;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
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
        translate.getTranslateManager().remove(this);
        LOG.I("关闭连接: " + this + " - "+ translate.getChannel()+"-->"+translate.getTerminalSocket());
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

