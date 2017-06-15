package client.translate;

import protocol.Command;
import protocol.Parse;
import utils.LOG;
import utils.MD5Util;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.DatagramChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

/**
 * Created by user on 2017/6/13.
 */
public class DataDownload extends DataImp{
    public DataDownload(DataElement element) {
        super(element);
    }

    @Override
    protected void sureMTU() {
        try {
            //接收MTU BUFFER
            initOverTime();
            ByteBuffer buffer = ByteBuffer.allocate(Parse.DATA_BUFFER_MAX_ZONE);
            SocketAddress address = null;
            while (isNotOverTime()){
                buffer.clear();
                address = getChannel().receive(buffer);
                if (address!=null){
                    buffer.flip();
                    LOG.I("确定MTU: "+ buffer);
                    if (buffer.get(0) == Command.UDPTranslate.mtuCheck){
                        //返回信息.
                        mtuValue = buffer.limit();
                        LOG.I("MTU : "+ mtuValue);
                        sendDataToAddress(buffer);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void slice() {
        super.slice();
        //发送切片成功,开始接收数据
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.clear();
        buffer.put(Command.UDPTranslate.sliceSure);
        buffer.flip();
        try {
            sendDataToAddress(buffer);
            LOG.I("切片信息:"+sliceUnitMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void translation() {
        initOverTime();
        try {
            initOverTime();
            state = SEND;
            //传输数据 - 异步发送
            AsynchronousFileChannel fileChannel =AsynchronousFileChannel.open(element.downloadFileTemp, StandardOpenOption.WRITE);
            while (getChannel().isOpen() && overTimeCount<OVER_MAX && state!=OVER){
                if (state == SEND){
                    querySend();
                }
                if (state == RECEIVE){
                    receiveData(fileChannel);
                }
                if (state == OVER){
                    LOG.I("传输完成");
                    //判断文件md5

                    String md5 = MD5Util.getFileMD5String(element.downloadFileTemp.toFile());
                    if (md5.equalsIgnoreCase(element.downloadFileMD5)){
                        closeFileChannel(fileChannel);
                        LOG.I("下载完成 - 文件MD5:"+ md5 +" , 源MD5" +element.downloadFileMD5+" ,从命名:"+element.downloadFileTemp.toFile().renameTo(element.downloadFile.toFile()));
                        //通知结束
                        ByteBuffer buffer = ByteBuffer.allocate(1);
                        buffer.clear();
                        buffer.put(Command.UDPTranslate.over);
                        buffer.flip();
                        sendDataToAddress(buffer);
                        closeFileChannel(fileChannel);
                    }else{
                        LOG.I("请求重传,数据异常.");
                        state = SEND;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void querySend() {

        ByteBuffer sendBuf = ByteBuffer.allocate(1);
        sendBuf.clear();
        sendBuf.put(Command.UDPTranslate.send);
        sendBuf.flip();
        try {
            sendDataToAddress(sendBuf);
            state = RECEIVE;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void receiveData(AsynchronousFileChannel fileChannel) {
        if (fileChannel==null || !fileChannel.isOpen()){
            return;
        }
        try {
            SocketAddress address;
            DatagramChannel channel = getChannel();
            ByteBuffer recBuf = null;
            int count;
            while (channel.isOpen()){
                recBuf = ByteBuffer.allocate(mtuValue);
                recBuf.clear();
                address = channel.receive(recBuf);
                if (address!=null ){
                    recBuf.flip();
                    if (recBuf.remaining()<4) continue;
                    count = recBuf.getInt();
                    LOG.I("收到: "+ recBuf+", 计数:"+count);
                    if (count>0) {
                        fileChannel.write(recBuf, sliceUnitMap.get(count), count, this);
                    }
                    if (count==-1){
                       if (sliceUnitMap.size()>0){
                           state = SEND;
                       }else{
                           state = OVER;
                       }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void completed(Integer integer, Object o) {
        //已写入
        sliceUnitMap.remove(o); //移除
        if (sliceUnitMap.size() == 0){
            state = OVER;
        }
    }
}
