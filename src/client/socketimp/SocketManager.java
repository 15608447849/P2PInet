package client.socketimp;

import client.obj.Info;
import client.sourceimp.SourceManager;
import protocol.Parse;
import utils.LOG;
import utils.NetworkUtil;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;

/**
 * Created by user on 2017/6/1.
 *  连接
 */
public class SocketManager implements CompletionHandler<Void, Void> {
    //重新连接时间
    private static final int RECONNECT_TIME = 1000 * 30;
    public Info info;
    public AsynchronousSocketChannel socket;
    public SocketHandler reader;
    public SocketCommand commander;
    public SourceManager sourceManager;
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(Parse.DATA_BUFFER_MAX_ZONE);
    private boolean isConnected;
    public TranslateManager translateManager = new TranslateManager();
    public SocketManager(Info info,SourceManager sourceManager){
        this.info = info;
        this.sourceManager = sourceManager;
        commander = new SocketCommand(this);
        reader = new SocketHandler(this,5);
        LOG.I("客户端创建成功 , 本地地址: "+info.getLocalAddress()+" 物理地址: "+ NetworkUtil.macByte2String(info.getLocalMac()));

    }
    //连接服务器
    public void connectServer(){
        try {
            if (translateManager.size() > 0){
                LOG.I("存在传输任务.暂不连接服务器.");
                reConnection();
            }else{
                socket = AsynchronousSocketChannel.open(AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(10)));
                socket.setOption(StandardSocketOptions.SO_KEEPALIVE,true);//保持连接
//            socket.setOption(StandardSocketOptions.SO_REUSEADDR,true);//端口复用
                socket.setOption(StandardSocketOptions.TCP_NODELAY,true);
                socket.bind(info.getLocalAddress());//绑定到本地
                socket.connect(info.getServerAddress(), null, this);
                PortManager.get().addPort(info.getLocalAddress().getPort());//添加已使用端口
                //等待
                synchronized (info){
                        info.wait();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void completed(Void aVoid, Void aVoid2) {
        synchronized (info){
            info.notifyAll();
        }
       isConnected = true;
       LOG.I("成功连接 - "+info.getServerAddress());
       reader.read(byteBuffer);
    }

    @Override
    public void failed(Throwable throwable, Void aVoid) {
        synchronized (info){
            info.notifyAll();
        }
        throwable.printStackTrace();
        //重新连接
        reConnection();
    }

    /**
     * 重新连接
     */
    public void reConnection() {
        closeConnect();
        try {
            synchronized (this){
                this.wait(RECONNECT_TIME);
            }
        } catch (InterruptedException e) {
        }
        connectServer();
    }

    public void closeConnect() {
        if (socket==null) return;
        try {
            socket.close();
            LOG.E("TCP CONNECTED CLOSE.");
        } catch (IOException e) {
        }finally {
            socket = null;
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
