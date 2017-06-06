package client.socketimp;

import client.obj.Info;
import client.sourceimp.SourceManager;
import protocol.Parse;
import utils.LOG;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashSet;
import java.util.concurrent.Executors;

/**
 * Created by user on 2017/6/1.
 *  连接
 */
public class SocketManager implements CompletionHandler<Void, Void> {


    public Info info;
    public AsynchronousSocketChannel socket;
    public SocketHandler reader;
    public SocketCommand commander;
    public SourceManager sourceManager;
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(Parse.buffSize);
    private boolean isConnected;
    public SocketManager(Info info,SourceManager sourceManager){
        this.info = info;
        this.sourceManager = sourceManager;
        commander = new SocketCommand(this);
        reader = new SocketHandler(this,5);
        LOG.I("客户端创建成功 ,服务器信息:"+info.getServerAddress()+" 本地地址:"+info.getLocalAddress());
    }
    public void connectServer(){
        try {
            socket = AsynchronousSocketChannel.open(AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(10)));
            socket.bind(info.getLocalAddress());//绑定到本地
            socket.setOption(StandardSocketOptions.SO_KEEPALIVE,true);
            socket.setOption(StandardSocketOptions.SO_REUSEADDR,true);
            socket.connect(info.getServerAddress(), null, this);
            PortManager.get().addPort(info.getLocalAddress().getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //等待
        synchronized (info){
            try {
                info.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void completed(Void aVoid, Void aVoid2) {
        synchronized (info){
            info.notifyAll();
        }
       isConnected = true;
        reader.read(byteBuffer);
    }

    @Override
    public void failed(Throwable throwable, Void aVoid) {
        synchronized (info){
            info.notifyAll();
        }
        throwable.printStackTrace();
        closeConnect();
        connectServer();
    }

    public void closeConnect() {
        try {
            socket.close();
            socket = null;
        } catch (IOException e) {

        }finally {
            isConnected = false;
        }
    }

    /**
     * 是否有效
     * @return
     */
    public boolean isValid(){
        return socket!=null && socket.isOpen() && isConnected;
    }

}
