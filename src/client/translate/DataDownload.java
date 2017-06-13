package client.translate;

import protocol.Command;
import utils.LOG;
import utils.MD5Util;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.DatagramChannel;
import java.nio.file.StandardOpenOption;

/**
 * Created by user on 2017/6/13.
 */
public class DataDownload extends DataImp implements CompletionHandler<Integer,Void>{
    public DataDownload(DataElement element) {
        super(element);
    }

    @Override
    protected boolean notifyUploader() {
        try {
            //通知下载
            element.buf2.clear();
            element.buf2.put(Command.UDPTranslate.resourceUpload);
            element.buf2.flip();
            for (int i= 0;i<3;i++){
                element.buf2.rewind();
                element.channel.send(element.buf2,element.toAddress);
            }
            LOG.I("发送上传通知.");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.notifyUploader();
    }

    @Override
    protected boolean translateDown() {
        LOG.I("等待接收中.");
        overTimeCount = OVER_INIT;
        position = 0L;
        recvCount=0L;//接收
        ByteBuffer sendbuf = element.buf2;
        DatagramChannel channel = element.channel;
        AsynchronousFileChannel fileChannel = null;

        try{
            AsynchronousFileChannel.open(element.downloadFileTemp, StandardOpenOption.WRITE);
            ByteBuffer buffer;
            //开始接收
            while (channel.isOpen() && overTimeCount<OVER_MAX){
                buffer = ByteBuffer.allocate(element.buf1.capacity());
                buffer.clear();

                if (  (channel.receive(buffer) )!= null){
                    buffer.flip();
                    //数据分析:
                    sendCount = buffer.getLong();
                    if (sendCount == -1L){
                        //传输结束
                        //判断文件MD5是否正确,不正确重新传输.
                        String md5 = MD5Util.getFileMD5String(element.downloadFileTemp.toFile());
                        if (md5.equalsIgnoreCase(element.downloadFileMD5)){
                            //返回
                            LOG.I("下载完成 - "+ md5 + element.downloadFileTemp);
                           break;
                        }
                    }else if (sendCount == (recvCount+1)){
                        //接收数据
                        position = buffer.getLong();
                        fileChannel.write(buffer,position,null,this);
                    }
                        //回执
                    sendbuf.clear();
                    recvCount=sendCount;
                    recvCount++;
                    sendbuf.putLong(recvCount);
                    sendbuf.flip();
                    channel.send(sendbuf,element.toAddress);
                    overTimeCount=OVER_INIT;
                }
                else{
//                    overTimeCount++;
                    synchronized (this){
                        this.wait(overTime);
                    }
                }
            }
            closeFileChannel(fileChannel);
            boolean f =  overTimeCount<OVER_MAX && element.downloadFileTemp.toFile().renameTo(element.downloadFile.toFile());
            LOG.I("返回结果 : "+f);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
           closeFileChannel(fileChannel);

        }
        return super.translateDown();
    }

    @Override
    public void completed(Integer integer, Void aVoid) {
        LOG.I(integer+" ");
    }

    @Override
    public void failed(Throwable throwable, Void aVoid) {

    }
}
