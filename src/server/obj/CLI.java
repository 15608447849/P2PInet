package server.obj;
import protocol.Excute;
import protocol.Intent;
import protocol.Parse;
import server.abs.IOperate;
import utils.LOG;
import utils.NetworkUtil;
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
public class CLI extends Thread implements CompletionHandler<Integer,ByteBuffer> {

    //管道
    private AsynchronousSocketChannel socket;
    //数据块
    private ByteBuffer byteBuffer;
    //操作者
    private IOperate operate;
    //网络地址
    private InetSocketAddress netSocket;
    //本机物理地址
    private byte[] localMac;
    //nat类型
    private int natType;
    //写
    private CLIWrite write;
    private boolean isExist;
    private int authentication;
    /**
     *  服务器用于判断此客户端是否存活的标识->
     *  在30秒之后还未更新,断开连接,删除客户端
     */
    private long time;
    //传输
    private Intent intent;
    /**
     *
     */
    public CLI(AsynchronousSocketChannel channel,Intent intent) throws IOException {
        this.setSocket(channel);
        this.intent = intent;
        this.intent.putCLI(this);
        this.byteBuffer = ByteBuffer.allocate(Parse.buffSize);
        this.operate = operate;
        this.write = new CLIWrite(this);
        //开始读取内容
        this.readContent();
        //如果30秒后,检测到非法,删除连接.
       this.start();
    }

    private void setSocket(AsynchronousSocketChannel socket) throws IOException {
        if (socket==null) throw new NullPointerException("AsynchronousSocketChannel is null.");
        this.socket = socket;
        this.netSocket = (InetSocketAddress) socket.getRemoteAddress();
    }

    public int getNatType() {
        return natType;
    }

    public void setNatType(int natType) {
        this.natType = natType;
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
                socket.shutdownOutput();
                socket.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            byteBuffer = null;
            time = 0;
            socket=null;
            operate = null;
            write = null;
            LOG.E(this + " 关闭.");
        }
        return false;
    }

    /**
     * 设置主机物理标识
     * @param macBytes
     */
    public void setMacAddress(byte[] macBytes) {
        this.localMac = macBytes;
    }
    /**
     * 通过netSocket比较是否相同
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o!=null && o instanceof CLI){
            CLI serverCLI = (CLI) o;
            if (this.getMac().equals(serverCLI.getMac())){
                //如果物理地址相同
               return serverCLI.netSocket.equals(this.netSocket); //比较网络地址
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        String host = getMac()+netSocket;
        return host.hashCode();
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");
        stringBuffer.append("物理地址: "+ getMac());
        stringBuffer.append(", 网络地址: "+ netSocket);
        stringBuffer.append(", 连接有效: "+ isValid());
        stringBuffer.append(", 更新时间:"+ getUpdateTimeString());
        stringBuffer.append(", HashCode:"+ hashCode());
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
        return NetworkUtil.macByte2String(localMac);
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
     * 是否认证
     * @return
     */
    public boolean isAuthentication() {
        return authentication==2;
    }
    /**
     * 是否发送认证消息
     */
    public boolean isSendAuthentication(){
        return authentication==1;
    }
    /**
     * 设置认证结果
     * 0 没有
     * 1 进行中
     * 2 完成
     */
    public void setAuthentication(int i){
        this.authentication = i;
    }
    /**
     * 是否存在客户端队列
     * @return
     */
    public boolean isExist() {
        return isExist;
    }

    public void setExist(boolean isExist){
        this.isExist = isExist;
    }

    /**
     * 连接合法性检测.
     */
    @Override
    public void run() {
        synchronized (this){
            try {
                this.wait(30 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!isAuthentication()){
                close();//关闭连接.
            }
        }
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
//            LOG.E(this+" 客户端断线.");
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

    public Intent getIntent() {
        return intent;
    }
}
