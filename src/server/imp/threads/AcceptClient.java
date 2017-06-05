package server.imp.threads;

import server.abs.IOperate;
import server.abs.IServer;
import server.abs.IThread;
import server.obj.ServerCLI;
import utils.LOG;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by user on 2017/5/31.
 */
public class AcceptClient extends IThread implements CompletionHandler<AsynchronousSocketChannel, Void>{

    private AsynchronousServerSocketChannel listener;
    public AcceptClient(IServer server) {
        super(server);
        this.listener = (AsynchronousServerSocketChannel) server.getParam("listener");
        LOG.I("打开接受客户端线程 :"+this);
    }

    @Override
    protected void action() {
        try {
            listener.accept(null,this); //接受一个连接
        } catch (Exception e) {}
    }
    @Override
    public void completed(AsynchronousSocketChannel asynchronousSocketChannel, Void aVoid) {
        action();//接受下一个服务
        //处理当前连接
        new ServerCLI(asynchronousSocketChannel,(IOperate)server.getParam("operate"));
    }

    @Override
    public void failed(Throwable throwable, Void aVoid) {
        LOG.I("接受到一个连接,失败 > "+ throwable.toString());
    }
}
