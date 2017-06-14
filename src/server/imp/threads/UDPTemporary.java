package server.imp.threads;

import client.obj.SerializeConnectTask;
import client.obj.SerializeSource;
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
    private ByteBuffer sendBuf;
    private ByteBuffer recBuffer;
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
                checkClose();
                synchronized (this){
                    try {
                        this.wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };



    public UDPTemporary(InetSocketAddress socketAddress, SerializeConnectTask task, IOperate operate, IThreadInterface manager) {
            this.socketAddress = socketAddress;
            this.connectTask = task;
            this.operate = operate;
            this.manager = manager;
            this.start();
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
            clientA = operate.getClientByMac(connectTask.getUploadHostMac());
            clientB = operate.getClientByMac(connectTask.getDownloadHostMac());

        if (clientA==null || clientB==null) {
            LOG.I("无法创建UDP连接中间请求,客户端不存在:\n"+clientA+"\n"+ clientB);
            return false;
        }
        //选择模式 -> 根据模式选择执行类
        switchMode();
        //获取执行类
        this.connectTask.setServerTempAddress(socketAddress);
        manager.putUseConnect(socketAddress.getPort(),this);
        recBuffer = ByteBuffer.allocate(Parse.UDP_DATA_MIN_BUFFER_ZONE);
        sendBuf = ByteBuffer.allocate(Parse.UDP_DATA_MIN_BUFFER_ZONE);
        selector = Selector.open();
        channel = DatagramChannel.open().bind(socketAddress);
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        sendMessage.start();

        LOG.I(this+"打开 UDP 连接端口 - "+ socketAddress+" 初始化完成.");
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
        manager.removePort(connectTask.getServerTempAddress().getPort());
        LOG.I("搭桥完成.关闭任务.");
    }

    private void notifyClient(CLI client) {
        client.getWrite().notifyConnect(Command.Server.queryClientConnectUDPService,connectTask);
    }
    //通知客户端A主动连接.
    public void notifyClientAConnect() {
        notifyClient(clientA);
        LOG.I(this + " 通知客户端A : "+clientA+" 主动连接UDP.");
    }
    //通知客户端B主动连接.
    protected void notifyClientBConnect() {
        notifyClient(clientB);
        LOG.I(this + " 通知客户端B : "+clientB+" 主动连接UDP.");
    }


    //处理请求(接受服务器的消息)
    private void receiveMessage() throws Exception {
        DatagramChannel sc;
        while (selector.isOpen() && selector.select() > 0){
            Iterator iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key;
                key = (SelectionKey) iterator.next();
                iterator.remove();
                if (key.isReadable()) {
                    recBuffer.clear();
                    sc = (DatagramChannel) key.channel();
                    handler((InetSocketAddress) sc.receive(recBuffer));
                }
            }
        }
    }

    private void handler(InetSocketAddress clientNatAddress) {
        recBuffer.flip();
        if (clientNatAddress==null || recBuffer.limit()==0) return;

        byte command = recBuffer.get(0);
        if (command == Command.UDPTranslate.udpHeartbeat){
            handlerClientHeartbeat(clientNatAddress);
        }else if (command == Command.UDPTranslate.serverHeartbeatResp){
            handlerClientReceiveResp(clientNatAddress);
        }
    }



    /**
     * 处理心跳
     */
    private void handlerClientHeartbeat(InetSocketAddress clientNatAddress){
        try {
            if (connectTask.getComplete() >= 3 ) return;

            byte[] mac = new byte[6];
            recBuffer.position(1);
            recBuffer.get(mac,0,6);
            String macStr = NetworkUtil.macByte2String(mac);

            if (macStr.equals(connectTask.getDownloadHostMac())){
                connectTask.setDownloadHostAddress(clientNatAddress);
            }else
            if (macStr.equals(connectTask.getUploadHostMac())){
                connectTask.setUploadHostAddress(clientNatAddress);
            }
            LOG.I(this + "收到 "+ clientNatAddress+" 心跳. 当前complete == "+connectTask.getComplete() );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //客户端 接收到 服务器的命令回应
    private void handlerClientReceiveResp(InetSocketAddress clientNatAddress) {
        byte[] mac = new byte[6];
        recBuffer.position(1);
        recBuffer.get(mac,0,6);
        String macStr = NetworkUtil.macByte2String(mac);
        if ( macStr.equals(clientA.getMac()) || macStr.equals(clientB.getMac())){
            connectTask.setComplete(connectTask.getComplete()+1);
        }
        LOG.I(this + "收到 "+ clientNatAddress+" 命令回应,当前 complete == "+connectTask.getComplete());
    }

    /**
     * 单独开启一个线程处理.信息的发送.
     */
    private void sendTerminalNatInfo() {
        if (connectTask.getComplete()>= 3 && connectTask.getComplete() <5) {//3.4
            //根据客户端A的mac - 找到 Net地址.
            LOG.I("互换消息 - "+ connectTask.getComplete());
            InetSocketAddress clientANat = getNatAddress(clientA.getMac());
            InetSocketAddress clientBNat = getNatAddress(clientB.getMac());
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
                modeB = 2;
            }
            if (mode == NetworkUtil.MODE_STMM_SYMM){
                //设置服务器中转数据模式
                modeA = modeB = 3;
            }
            try {
                //返回回执. 带了对方的NAT信息,并且,发送了客户端的模式(主动,被动)
                sendTranlslate(modeA,clientANat,clientBNat);
                sendTranlslate(modeB,clientBNat,clientANat);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检测是否关闭
     */
    private void checkClose() {
        if (connectTask.getComplete() >= 5){
            closeTask();
        }
    }

    //写对象
    private void sendTranlslate(int mode, InetSocketAddress sendTo, InetSocketAddress terminalAddress) throws IOException {
        SerializeTranslate translate = new SerializeTranslate(terminalAddress,mode);
        sendBuf.clear();
        sendBuf.put(Command.UDPTranslate.serverHeartbeatResp);
        byte[] objBytes =  Parse.sobj2Bytes(translate);
        LOG.E("UDP - 传输对象长度:"+ objBytes.length);
        sendBuf.put(objBytes);
        sendBuf.flip();
        channel.send(sendBuf,sendTo);
    }

    private InetSocketAddress getNatAddress(String mac) {
        InetSocketAddress address = null;
        try {
            if (mac.equals(connectTask.getDownloadHostMac())){
                    address = connectTask.getDownloadHostAddress();
                }
            if (mac.equals(connectTask.getUploadHostMac())){
                address = connectTask.getUploadHostAddress();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return address;
    }

}
