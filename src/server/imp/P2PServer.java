package server.imp;

import server.abs.IOperate;
import server.abs.IParameter;
import server.abs.IServer;
import server.abs.IThread;
import server.imp.threads.AcceptClient;
import server.obj.ServerInfo;
import utils.LOG;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by user on 2017/5/31.
 */
public class P2PServer implements IServer {
    //初始化参数
    public ServerInfo param;
    //异步连接socket
    public final AsynchronousServerSocketChannel listener;
    //是否绑定
    public boolean isBind;
    //管理得线程
    public final HashMap<String,IThread> threadMap = new HashMap<>();
    public IOperate operate;
    public P2PServer(int threadNunber) throws IOException {
        listener = AsynchronousServerSocketChannel.open(AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(threadNunber)));
    }


    @Override
    public void initServer(IParameter parameter) {
        this.param = (ServerInfo) parameter;
        try {
            listener.bind(this.param.localAddress_1);
            isBind = true;
            LOG.I("服务器绑定 -> "+this.param.localAddress_1 + ", "+param.getMac());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectServer(IOperate operate) {
            this.operate = operate;
    }

    @Override
    public void startServer() {
        //创建接受线程
        IThread accept = new AcceptClient(this);
        accept.launch();
        threadMap.put("AcceptClient",accept);
        LOG.I("服务器启动");
        synchronized (this){
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void stopServer() {

    }

    @Override
    public Object getParam(String name) {
        if (name.equals("listener")) return listener;
        if (name.equals("operate")) return operate;
        return null;
    }

}
