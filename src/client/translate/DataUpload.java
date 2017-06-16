package client.translate;

import com.sun.org.apache.regexp.internal.RE;
import protocol.Command;
import protocol.Parse;
import utils.LOG;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by user on 2017/6/13.
 * 确定 MTU
 * 确定分片信息  :    mtu = 1400 ,文件大小: 14008 ->  14008/1400 = 10次 余下8byte, > 11次 ,每次的下标: (0,0) (1,1400) ....(10,1400*10),(11,(1400*10)+8)
 */
public class DataUpload extends DataImp {


    private ArrayList<Integer> receiveSuccessIndexList = new ArrayList<>();

    public DataUpload(DataElement element) {
        super(element);
    }

    @Override
    protected void sureMTU() {
        LOG.I("检测MTU值.");
        resetTime();
        int currentMTU = Parse.DATA_BUFFER_MAX_ZONE;//MTU最大单位数量.
        final ByteBuffer buf = ByteBuffer.allocate(currentMTU);
        SocketAddress address = null;
        while(isNotTimeout()){
            if (currentMTU>Parse.UDP_DATA_MIN_BUFFER_ZONE){
                buf.clear();
                for (int i = 0;i<currentMTU;i++){
                    buf.put(Command.UDPTranslate.mtuCheck);
                }
                buf.flip();

                    sendDataToAddress(buf);

                currentMTU--;
            }

            //接收
            buf.clear();
            try {
                address = getChannel().receive(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (checkAddress(address)){
                buf.flip();

                if (buf.get(0) == Command.UDPTranslate.mtuCheck){
                    mtuValue = buf.limit();
                    LOG.I("确定MTU大小: "+ mtuValue);
                    return;
                }
            }
        }
        LOG.I("检测MTU超时.");
        state = ERROR;
    }


    @Override
    protected void slice() {
        if (state==ERROR) return;
        super.slice();
        resetTime();
        LOG.I("等待客户端完成分片操作.");
        final ByteBuffer buffer = ByteBuffer.allocate(1);
        SocketAddress address = null;
         while (isNotTimeout()){
           buffer.clear();
           try {
                address = getChannel().receive(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
                if (checkAddress(address)){
                    buffer.flip();
                    if (buffer.get(0) == Command.UDPTranslate.sliceSure){
                        LOG.I("对方 - [ " + address+" ] ,切片已完成. 开始传输数据.");
                        return;
                    }
                }
            }
            LOG.I("切片等待超时.");
            state = ERROR;
    }

    @Override
    protected void translation() {
        if (state==ERROR) return;
        resetTime();
        state = RECEIVE;
        //传输数据 - 异步发送
        AsynchronousFileChannel fileChannel = null;
        try {
            fileChannel = AsynchronousFileChannel.open(element.uploadFilePath, StandardOpenOption.READ);
            while (state!=OVER &&  state!=ERROR){
                if (state == SEND){
                    sendData(fileChannel);
                }
                if (state == RECEIVE){
                    receiveData();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            closeFileChannel(fileChannel);
        }
    }
    /**
     * 发送数据
     * @param fileChannel
     */
    private void sendData(AsynchronousFileChannel fileChannel) {
        if (fileChannel==null || !fileChannel.isOpen() ||  !getChannel().isOpen()){
            state = ERROR;
            return;
        }
        LOG.I("可移除分片数 : "+receiveSuccessIndexList.size());
        //处理需要发送的分片
        Iterator<Integer> itr = receiveSuccessIndexList.iterator();
        while (itr.hasNext()){
            sliceUnitMap.remove(itr.next());
            itr.remove();
        }
        ByteBuffer sendBuf = null;

        long time = System.currentTimeMillis();
        int i = 0;
        for (Integer count:sliceUnitMap.keySet()){

            sendBuf = ByteBuffer.allocate(mtuValue);
            sendBuf.clear();
            sendBuf.putInt(count);
            fileChannel.read(sendBuf,sliceUnitMap.get(count),sendBuf,this);
            i++;
//            waitTime2();
            if (i > 10) break;
        }
        LOG.I("可发送分片数量 : "+ sliceUnitMap.size()+"  实际发送量 : "+i);
        sendBuf = ByteBuffer.allocate(mtuValue);
        //发送完成标识
        sendBuf.clear();
        sendBuf.putInt(-1);
        fileChannel.read(sendBuf,element.fileLength,sendBuf,this);
        state = RECEIVE;// 接收状态.接收对方回执
        LOG.I("数据流已发送完毕.耗时: "+ (System.currentTimeMillis() - time));
    }
    //读取本地流发送成功
    @Override
    public void completed(Integer integer, Object o) {
            ByteBuffer sendBuf = (ByteBuffer) o;
            sendBuf.flip();
            //发送.
            sendDataToAddress(sendBuf);
    }

    /**
     * 接受数据流
     */
    private void receiveData() {
        resetTime();
        final ByteBuffer recBuf = ByteBuffer.allocate(mtuValue);
        SocketAddress address = null;
        int index = -1;
        while (state==RECEIVE  && getChannel().isOpen() ){
            recBuf.clear();
            try {
                address = getChannel().receive(recBuf);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (checkAddress(address)){
                resetTimeAndCount();
                recBuf.flip();
                if (recBuf.get(0) == Command.UDPTranslate.over){
                    LOG.I(" 收到 结束任务.");
                    state = OVER;
                }else
                if (recBuf.get(0) == Command.UDPTranslate.receiveSlice){
                    recBuf.position(1);
                    while (recBuf.remaining()>=4){
                        receiveSuccessIndexList.add(recBuf.getInt());
                    }

                }else if (recBuf.get(0) == Command.UDPTranslate.send){
                    LOG.I(" 收到 发送数据请求..");
                    state = SEND;
                }

            }
            if (!isNotTimeout()){
                if (overTimeCount>OVER_MAX){
                    LOG.E("接受数据应答超时.");
                    state = ERROR;
                }else{
                    resetTime();
                    overTimeCount++;
                }

            }
        }


    }



}
