package client.translate;

import protocol.Parse;
import utils.LOG;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by user on 2017/6/13.
 */
public abstract class DataImp extends Thread implements CompletionHandler<Integer,Object> {


   public static final int OVER_TIME_ONCE = 200; //单次超时时间 1毫秒(ms)=1 000 000纳秒(ns) 1毫秒
   public static final int OVER_TIME_ONCE_2 = 200; //单次超时时间 1毫秒(ms)=1 000 000纳秒(ns) 1/10毫秒
    public static final int OVER_MAX = 10; //超时时间 最大次数
    protected int overTimeCount = 0;//超时次数
    protected DataElement element;
    protected TranslateAction action;

    public int mtuValue = Parse.UDP_DATA_MIN_BUFFER_ZONE;
    public static final int INDEX_LEN = 4;
    public HashMap<Integer,Long> sliceUnitMap = null;

    public static final int ERROR = -1;
    public static final int NODE = 0;
    public static final int SEND = 1;
    public static final int RECEIVE = 2;
    public static final int OVER = 3;

    public int state = NODE;

    public long time_limit = 30 * 1000;//30秒
    public long curTime = System.currentTimeMillis();


    public DataImp(DataElement element) {
        this.element = element;
        time_limit = element.type == DataElement.UPLOAD?5000:3000;
    }
    public DataImp setAction(TranslateAction action) {
        this.action = action;
        return this;
    }
    @Override
    public void run() {
        //判断类型
        if (element.type == DataElement.UPLOAD || element.type == DataElement.DOWNLOAD ){
            //确定mtu大小
            sureMTU();
            //确定分片数据
            slice();
            //传输文件
            translation();
        }
        if (action!=null){
            action.onComplete(element);
        }
    }
    protected abstract void sureMTU();
    protected void slice(){
     if (mtuValue>0){setSliceMap();}
    }
    protected abstract void translation();




    protected void waitTime(){
        try {
            TimeUnit.MICROSECONDS.sleep(OVER_TIME_ONCE);
        } catch (InterruptedException e) {
        }
    }
    protected void waitTime2(){
        try {
            TimeUnit.MICROSECONDS.sleep(OVER_TIME_ONCE_2);
        } catch (InterruptedException e) {
        }
    }
    protected void waitTime(long time){
        synchronized (this){
            try {
                this.wait(time * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void closeFileChannel(AsynchronousFileChannel fileChannel){
        if (fileChannel!=null && fileChannel.isOpen()){
            try {
                fileChannel.close();
                LOG.I("关闭文件流 - "+ fileChannel.isOpen());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 计算分片单元格数量
     * @return
     *
     */
    public HashMap<Integer,Long> cellCount(long length, int mtu){
        long remainder = length%mtu;
        int count = (int) ((length-remainder)/mtu);
        HashMap<Integer,Long> map = new HashMap<>();
        for (int index = 0; index < count; index++ ){
            map.put(index, (long) (index*mtu));
        }
        if (remainder>0){
            map.put(count, (long) (count*mtu));
        }
        return map;
    }

    //发送数据到对方
    protected void sendDataToAddress(ByteBuffer buffer){
        try {
            element.sendBuffer(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            LOG.E("数据:"+ buffer);
        }
    }

    protected void resetTime(){
        curTime = System.currentTimeMillis();
    }
    protected boolean isNotTimeout(){
        return System.currentTimeMillis() - curTime <= time_limit;
    }

    protected DatagramChannel getChannel(){
        return element.channel;
    }
    protected void setSliceMap(){
        sliceUnitMap = cellCount(element.fileLength,mtuValue-INDEX_LEN);
    }
    protected boolean checkAddress(SocketAddress address){
        return address!=null && address.equals(element.toAddress);
    }
    protected void resetTimeAndCount() {
        resetTime();
        overTimeCount = 0;
    }
    @Override
    public void failed(Throwable throwable, Object o) {
        throwable.printStackTrace();
    }


}
