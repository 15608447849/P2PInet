package server.obj;
import protocol.Command;
import protocol.Excute;
import protocol.Intent;
import protocol.Parse;
import server.abs.IOperate;
import utils.LOG;
import utils.NetUtil;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * Created by user on 2017/6/1.
 * 客户端在服务器上的实体,负责接受数据
 */
public class ServerCLI implements CompletionHandler<Integer,ByteBuffer> {

    //管道
    private AsynchronousSocketChannel socket;
    //数据块
    private ByteBuffer byteBuffer;
    //操作者
    private IOperate operate;
    //网络地址
    private InetSocketAddress netSocket;
    //本机地址
    private InetSocketAddress localSocket;
    //本机物理地址
    private byte[] localMac;
    private CLIWrite write;
    /**
     *  服务器用于判断此客户端是否存活的标识->
     *  在30秒之后还未更新,断开连接,删除客户端
     */
    private long time;

    /**
     *
     * @param socket 这个客户端的socket
     * @param operate
     */
    public ServerCLI(AsynchronousSocketChannel socket, IOperate operate) {

        setSocket(socket);
        byteBuffer = ByteBuffer.allocate(Parse.buffSize);
        this.operate = operate;
        operate.putCLI(this);
        write = new CLIWrite(this);
        //发送共享目录资源上传命令
        readContent();
        LOG.I("接收到一个连接,创建客户端 - "+ this);
    }

    private void setSocket(AsynchronousSocketChannel socket){
        if (socket==null) throw new NullPointerException("AsynchronousSocketChannel is null.");
        this.socket = socket;
        try {
            this.netSocket = (InetSocketAddress) socket.getRemoteAddress();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * 是否是有效连接
     * @return
     */
    public boolean isValid() {
        if (socket!=null && socket.isOpen()) return true;
        return false;
    }
    public boolean close(){
        try {
            if (isValid()){
                socket.shutdownInput();
//                LOG.E("关闭读取.");
                socket.shutdownOutput();
//                LOG.E("关闭写入.");
                socket.close();
//                LOG.E("关闭通道.");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            byteBuffer = null;
            time = 0;
            socket=null;
        }

        return false;
    }

    /**
     * 设置主机本机信息
     * @param localSocket
     * @param macBytes
     */
    public void setLocal(InetSocketAddress localSocket, byte[] macBytes) {
        this.localSocket = localSocket;
        this.localMac = macBytes;
    }
    /**
     * 通过netSocket比较是否相同
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o!=null && o instanceof ServerCLI){
            ServerCLI serverCLI = (ServerCLI) o;
            return serverCLI.netSocket.equals(this.netSocket);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return netSocket.hashCode();
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");
        stringBuffer.append("物理地址: "+ NetUtil.macByte2String(localMac));
        stringBuffer.append(", 网络地址: "+ netSocket);
        stringBuffer.append(", 主机地址: "+ localSocket);
        stringBuffer.append(", 连接有效: "+ isValid());
        stringBuffer.append(", 更新时间:"+ getUpdateTimeString());
        stringBuffer.append("]");
        return stringBuffer.toString();
    }

    /**
     * 更新时间
     */
    public void updateTime() {
        time = System.currentTimeMillis();

    }

    /**
     * 获取更新时间
     * @return
     */
    public long getUpdateTime() {
        return time;
    }

    //最后更新时间字符串
    public String getUpdateTimeString() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(time);
    }

    /**
     * 获取mac地址
     * @return
     */
    public String getMac() {
        return NetUtil.macByte2String(localMac);
    }

    /**
     * 读取消息
     */
    private void readContent() {
        if (isValid()){
            byteBuffer.clear();
            socket.read( byteBuffer,  byteBuffer,this);
        }
    }

    /**
     * 获取写
     * @return
     */
    public CLIWrite getWrite() {
        return write;
    }

    /**
     * 获取通道
     * @return
     */
    public AsynchronousSocketChannel getSocket() {
        return socket;
    }



















    /**
     * 读取到数据OS回调
     * @param integer
     * @param byteBuffer
     */
    @Override
    public void completed(Integer integer, ByteBuffer byteBuffer) {
//      LOG.I("读取到: "+integer+" - "+byteBuffer);
        if (integer == -1){
            LOG.E(this+" 客户端断线.");
            close();
        }else{
            //处理
            HashMap<String,Object> map = Parse.message(byteBuffer);
            readContent();
            handlerMessage(map);
        }
    }

    @Override
    public void failed(Throwable throwable, ByteBuffer byteBuffer) {
//        throwable.printStackTrace();
//        LOG.E(this+" 错误: "+ throwable.getMessage());
    }


    private boolean handlerMessage(HashMap<String, Object> map) {
        if (map==null) return false;
        return Excute.handlerMessage(Excute.SERVER,new Object[]{map,this});
    }

    public IOperate getOperate() {
        return operate;
    }
}
