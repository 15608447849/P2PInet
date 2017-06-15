package client.translate;

import protocol.Command;
import protocol.Parse;
import utils.LOG;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.DatagramChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

/**
 * Created by user on 2017/6/13.
 * 确定 MTU
 * 确定分片信息  :    mtu = 1400 ,文件大小: 14008 ->  14008/1400 = 10次 余下8byte, > 11次 ,每次的下标: (0,0) (1,1400) ....(10,1400*10),(11,(1400*10)+8)
 */
public class DataUpload extends DataImp {
    public DataUpload(DataElement element) {
        super(element);
    }

    @Override
    protected void sureMTU() {
        initOverTime();
        int currentMTU = Parse.DATA_BUFFER_MAX_ZONE;//MTU最大单位数量.
        ByteBuffer buf = ByteBuffer.allocate(currentMTU);
        SocketAddress address = null;
        while(isNotOverTime()){

            if (currentMTU>Parse.UDP_DATA_MIN_BUFFER_ZONE){
                buf.clear();
                for (int i = 0;i<currentMTU;i++){
                    buf.put(Command.UDPTranslate.mtuCheck);
                }
                buf.flip();
                try {
                    sendDataToAddress(buf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                currentMTU--;
            }
            waitTime();
            //接收
            buf.clear();
            try {
                address = getChannel().receive(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (address!=null){
                buf.flip();
                LOG.I("确定MTU大小: "+ buf);
                if (buf.get(0) == Command.UDPTranslate.mtuCheck){
                    mtuValue = buf.limit();
                    LOG.I("MTU: "+ mtuValue);
                    return;
                }
            }else{
                overTimeCount++;
            }
            waitTime();
        }


    }


    @Override
    protected void slice() {
        super.slice();
        try {
            LOG.I("等待客户端分片单元格.");
            initOverTime();
            ByteBuffer buffer = ByteBuffer.allocate(1);
            SocketAddress address;
            while (isNotOverTime()){
                buffer.clear();
                address = getChannel().receive(buffer);
                if (address!=null){
                    buffer.flip();
                    if (buffer.get(0) == Command.UDPTranslate.sliceSure){
                        LOG.I("对方 -" + address+" ,切片完成. 开始传输数据.");
                        return;
                    }
                }else{
                    waitTime();
                    overTimeCount++;
                }
            }
            LOG.I("超时.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void translation() {
        try {
            initOverTime();
            state = RECEIVE;
            //传输数据 - 异步发送
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(element.uploadFilePath, StandardOpenOption.READ);
            while (getChannel().isOpen() && overTimeCount<OVER_MAX && state!=OVER){
                if (state == SEND){
                    sendData(fileChannel);
                }
                if (state == RECEIVE){
                    receiveData();
                }
                if (state == OVER){
                    LOG.I("传输完成");
                    closeFileChannel(fileChannel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void sendData(AsynchronousFileChannel fileChannel) {
        if (fileChannel==null || !fileChannel.isOpen()){
            return;
        }
        ByteBuffer sendBuf = null;
        long time = System.currentTimeMillis();
        // 一毫秒 发 10次, 每次1kb -> 10 k/毫秒  = 10*1000 => 10000 kb/s ?
        //实际 1秒 8~10次, 8kb/s~10k/s ?
        // 1秒 100次 100kb/s
        for (Integer count:sliceUnitMap.keySet()){
            sendBuf = ByteBuffer.allocate(mtuValue);
            sendBuf.clear();
            sendBuf.putInt(count);
            fileChannel.read(sendBuf,sliceUnitMap.get(count),sendBuf,this);
            waitTime2();
        }
        //发送完成标识
        sendBuf.clear();
        sendBuf.putInt(-1);
        fileChannel.read(sendBuf,element.fileLength,sendBuf,this);
        state = RECEIVE;// 接收状态.接收对方回执
        LOG.E("已发送完毕.");
        LOG.E("耗时: "+ (System.currentTimeMillis() - time));
    }

    @Override
    public void completed(Integer integer, Object o) {
        try {

            ByteBuffer sendBuf = (ByteBuffer) o;
            sendBuf.flip();
//            int count = sendBuf.getInt();
//            try {
//                LOG.I(" 发送: "+sendBuf+" ,长度:"+integer+" - count:"+ count+" ,position: "+sliceUnitMap.get(count));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            sendBuf.rewind();
            //发送.
            sendDataToAddress(sendBuf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveData() {
        ByteBuffer recBuf = ByteBuffer.allocate(1);
        recBuf.clear();
        try {
            SocketAddress address = getChannel().receive(recBuf);
            if (address!=null){
                initOverTime();
                recBuf.flip();
                if (recBuf.get(0) == Command.UDPTranslate.send){
                    LOG.E(" 准备发送数据");
                    state = SEND;
                }else if (recBuf.get(0) == Command.UDPTranslate.over){
                    LOG.E(" 结束任务");
                    state = OVER;
                }
            }else{
                waitTime(1);//休眠一秒 - > 最大30次 => 超时时间30秒
                overTimeCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





}
