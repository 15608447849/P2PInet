package server.imp.threads;

import protocol.Intent;
import server.abs.IOperate;
import server.abs.IServer;
import server.abs.IThread;
import server.obj.CLI;
import server.obj.IParameter;
import utils.LOG;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by user on 2017/5/31.
 */
public class AcceptClient extends IThread implements CompletionHandler<AsynchronousSocketChannel, Void>{

    private AsynchronousServerSocketChannel listener;
    private IOperate operate;
    private IParameter parameter;
    public AcceptClient(IServer server) {
        super(server);
        this.operate = (IOperate)server.getParam("operate");
        this.parameter = (IParameter)server.getParam("param");
        this.listener = (AsynchronousServerSocketChannel) server.getParam("listener");
        launch();
        LOG.I("等待客户端接入服务 ,启动.");
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
        try {
            new CLI(asynchronousSocketChannel,new Intent(operate,parameter));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable throwable, Void aVoid) {
        LOG.I("接受到一个连接,失败 > "+ throwable.toString());
    }
}
