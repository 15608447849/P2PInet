package server.imp.servers;

import server.abs.*;
import server.imp.threads.AcceptClient;
import server.imp.threads.AuthenticationClientAuxiliary;
import server.imp.threads.NetAuthenticationHelper;
import server.imp.threads.UDPConnect;
import server.obj.IParameter;
import utils.LOG;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.HashMap;
import java.util.concurrent.Executors;

/**
 * Created by user on 2017/5/31.
 */
public class P2PServer implements IServer {
    //初始化参数
    private IParameter param;
    //异步连接socket
    private AsynchronousServerSocketChannel listener;
    //管理得线程
    private final HashMap<String,IThread> threadMap = new HashMap<>();
    /**
     * 操作手
     */
    private IOperate operate;

    /**
     * UDP管理
     */
    private IThreadInterface udpManage;
    public P2PServer() throws IOException {

    }


    @Override
    public void initServer(IParameter parameter) {
        this.param =  parameter;
    }

    @Override
    public void connectServer(IOperate operate) {
            this.operate = operate;
            this.operate.setServer(this);
    }

    @Override
    public  void createUdpManager(int startPort,int endPort){
        if (udpManage==null) udpManage = new UDPConnect(this);
        udpManage.setPort(startPort,endPort);
    }

    @Override
    public void startServer() {
        try {
            if (param.serverType == 0){
                listener = AsynchronousServerSocketChannel.open(AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(param.threadNunber)));
                //打开UDP认证服务
                listener.bind(this.param.tcpLocalAddress);
                LOG.I("服务器TCP 绑定 -> "+this.param.tcpLocalAddress);
                //创建接受线程
                threadMap.put("AcceptClient", new AcceptClient(this));
                threadMap.put("AuthenticationClient", new NetAuthenticationHelper(this));
                synchronized (this){
                    this.wait();
                }
            }
           if (param.serverType == 1){
               threadMap.put("AuthenticationClientAuxiliary", new AuthenticationClientAuxiliary(this));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopServer() {

    }

    @Override
    public Object getParam(String name) {
        if (name.equals("listener")) return listener;
        if (name.equals("operate")) return operate;
        if (name.equals("param")) return param;
        if (name.equals("udp")) return udpManage;
        return null;
    }

}
