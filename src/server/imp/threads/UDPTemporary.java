package server.imp.threads;

import client.obj.SerializeConnectTask;
import client.obj.SerializeTranslate;
import protocol.Command;
import protocol.Parse;
import server.abs.IOperate;
import server.abs.IThreadInterface;
import server.obj.CLI;
import utils.LOG;
import utils.NetworkUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * Created by user on 2017/6/6.
 */
public class UDPTemporary extends Thread{

    public SerializeConnectTask connectTask;
    //本地socket地址
    private InetSocketAddress socketAddress;
    public Selector selector = null;
    public DatagramChannel channel;
    public CLI clientA;
    public CLI clientB;
    private IThreadInterface manager;
    private ByteBuffer buffer;
    private IOperate operate;
    private int mode;


    /**
     * 发送消息的处理
     */
    private Thread sendMessage = new Thread(){
        @Override
        public void run() {
            //发送连接请求 ,通知AB 连接服务器
            notifyClientAConnect();
            notifyClientBConnect();
            while (channel!=null && channel.isOpen()){
                sendTerminalNatInfo();
            }
        }
    };

    public UDPTemporary(InetSocketAddress socketAddress, SerializeConnectTask task, IOperate operate, IThreadInterface manager) {
            this.socketAddress = socketAddress;
            this.connectTask = task;
            this.operate = operate;
            this.manager = manager;
            start();
    }
    @Override
    public void run() {
        try {
            //初始化
            if (!initService()) return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            receiveMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //初始化
    private boolean initService() throws Exception {

            clientA = operate.getClientByMac(connectTask.getDestinationMac());
            LOG.I("获取到 资源客户端 A:"+clientA);
            clientB = operate.getClientByMac(connectTask.getSourceMac());
            LOG.I("获取到 请求客户端 B:"+clientB);

        if (clientA==null || clientB==null) {
            //LOG.I("无法创建UDP连接中间请求,客户端不存在 - "+clientA+" <> "+ clientB);
            return false;
        }
        //选择模式 -> 根据模式选择执行类
        switchMode();
        //获取执行类
        this.connectTask.setServerTempUDP(socketAddress.getAddress().getAddress(),socketAddress.getPort());
        this.connectTask.setComplete(1);
        manager.putUseConnect(socketAddress.getPort(),this);
        buffer = ByteBuffer.allocate(Parse.buffSize);
        selector = Selector.open();
        channel = DatagramChannel.open().bind(socketAddress);;
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        LOG.I(this+"打开 UDP 连接端口 - "+ socketAddress);
        sendMessage.start();
        return true;
    }

    /**
     选择模式
     情况0 a-not_nat 或者 b not_nat ->
     情况1 a-full b-full ,通知A连接,a进入接收连接模式.返回A地址给B,b->a;
     情况2 af-full b-symm,通知a连接,通知b连接a.同时发送标识,receive给a,让它接收a
     情况3 a-symm b-full :  通知a连接, 让a直接发信息给b.同时告知b接收.
     情况2 a-symm bs-ymm ,代为进入待转发模式 ,通知A B向我发送数据, 收a->b,收b->a;
     * */
    private void switchMode() {
        mode = NetworkUtil.bitwise(clientA.getNatType(),clientB.getNatType());
    }

    //结束任务
    public void closeTask() {

        //关闭socket,返回端口
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            manager.removePort(connectTask.getServerTempUDP().getPort());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        LOG.I("搭桥完成.关闭任务.");
    }

    private void notifyClient(CLI client) {
        client.getWrite().notifyConnect(Command.Server.queryClientConnectUDPService,connectTask);
    }
    //通知客户端A主动连接.
    public void notifyClientAConnect() {
        notifyClient(clientA);
        LOG.I(this + " 通知客户端A :"+clientA+" 主动连接UDP.");
    }
    //通知客户端B主动连接.
    protected void notifyClientBConnect() {
        notifyClient(clientA);
        LOG.I(this + " 通知客户端A :"+clientA+" 主动连接UDP.");
    }























    //处理请求
    private void receiveMessage() throws Exception {
        while (selector.isOpen() && selector.select() > 0){
            Iterator iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key;
                key = (SelectionKey) iterator.next();
                iterator.remove();
                if (key.isReadable()) {
                    DatagramChannel sc = (DatagramChannel) key.channel();
                    buffer.clear();
                    handler((InetSocketAddress) sc.receive(buffer));
                }
            }
        }
    }

    private void handler(InetSocketAddress clientNatAddress) {
        buffer.flip();
        byte command = buffer.get(0);
        if (command == Command.UDPTranslate.udpHeartbeat){
            handlerClientHeartbeat(clientNatAddress);
        }
        if (command == Command.UDPTranslate.clientReceiveResp){
            handlerClientReceiveResp(clientNatAddress);
        }
    }



    /**
     * 处理心跳
     */
    private void handlerClientHeartbeat(InetSocketAddress clientNatAddress){
        try {
            if (connectTask.getCompele() == 3 ) return;
            LOG.I(this + "收到 "+ clientNatAddress+" 心跳." );
            byte[] mac = new byte[6];
            buffer.position(1);
            buffer.get(mac,0,6);
            String macStr = NetworkUtil.macByte2String(mac);

            if (macStr.equals(connectTask.getSourceMac())){
                //设置资源 源头
                connectTask.setSrcNET(clientNatAddress);
            }
            if (macStr.equals(connectTask.getDestinationMac())){
                //设置资源 需求者
                connectTask.setDesNET(clientNatAddress);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //客户端 接收到 服务器的命令回应
    private void handlerClientReceiveResp(InetSocketAddress clientNatAddress) {
        if (connectTask.getCompele() == 5) return;
        LOG.I(this + "收到 "+ clientNatAddress+" 命令回应." );
        byte[] mac = new byte[6];
        buffer.position(1);
        buffer.get(mac,0,6);
        String macStr = NetworkUtil.macByte2String(mac);
        if ( macStr.equals(clientA.getMac()) || macStr.equals(clientB.getMac())){
            connectTask.setComplete(connectTask.getCompele()+1);
        }
    }

    /**
     * 单独开启一个线程处理.信息的发送.
     */
    private void sendTerminalNatInfo() {
        if (connectTask.getCompele() < 3 || connectTask.getCompele() > 5) return;
        //根据客户端A的mac - 找到 Net地址.

        InetSocketAddress clientANat = getNatAddress(clientA.getMac());
        InetSocketAddress clientBnat = getNatAddress(clientB.getMac());
        if ( clientANat==null || clientANat==null ) return;
        int modeA = 0;
        int modeB = 0;
        //根据客户端 - 判断模式.
        if (mode == NetworkUtil.MODE_NOTNAT_NOTNAT
                || mode== NetworkUtil.MODE_NOTNAT_FULL
                || mode == NetworkUtil.MODE_NOTNAT_SYMM
                || mode == NetworkUtil.MODE_FULL_FULL
                || mode == NetworkUtil.MODE_FULL_NOTNAT
                || mode == NetworkUtil.MODE_FULL_SYMM
                ){
            //设置 A被动模式.
            modeA = 2;
            //设置 B主动模式.
            modeB = 1;
        }
        if (mode == NetworkUtil.MODE_SYMM_NOTNAT || mode == NetworkUtil.MODE_SYMM_FULL){
            //设置A主动模式
            modeA = 1;
            //设置B被动模式
            modeA = 2;
        }
        if (mode == NetworkUtil.MODE_STMM_SYMM){
            //设置服务器中转数据模式
            modeA = modeB = 3;
        }

        SerializeTranslate trans = new SerializeTranslate();
        trans.connectTask = connectTask;
        try {
          trans.mode = modeA;
            //返回回执. 带了对方的NAT信息,并且,发送了客户端的模式(主动,被动)
          sendObject(trans,clientANat);
          trans.mode = modeB;
          sendObject(trans,clientBnat);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendObject(Object object,InetSocketAddress address) throws IOException {
        buffer.clear();
        buffer.put(Command.UDPTranslate.serverHeartbeatResp);
        buffer.put(Parse.sobj2Bytes(object));
        buffer.flip();
        channel.send(buffer,address);
    }

    private InetSocketAddress getNatAddress(String mac) {
        InetSocketAddress address = null;
        try {
            if (mac.equals(connectTask.getSourceMac())){
                    address = connectTask.getSrcNET();
                }
            if (mac.equals(connectTask.getDestinationMac())){
                address = connectTask.getDesNet();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return address;
    }

}
