package client.translate;

import protocol.Command;
import protocol.Parse;
import utils.LOG;
import utils.MD5Util;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.DatagramChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

/**
 * Created by user on 2017/6/13.
 */
public class DataDownload extends DataImp implements CompletionHandler<Integer,Void>{
    public DataDownload(DataElement element) {
        super(element);
    }

    @Override
    protected boolean translation() {
        LOG.I("等待接收中.");
        overTimeCount = OVER_INIT;
        position = 0L;
        recvCount=0L;//接收
        ByteBuffer sendbuf = element.buf2;
        ByteBuffer buffer = element.buf1;
        DatagramChannel channel = element.channel;
        AsynchronousFileChannel fileChannel = null;
        int count = 0;
        SocketAddress address;
        try{
            fileChannel = AsynchronousFileChannel.open(element.downloadFileTemp, StandardOpenOption.WRITE);

            //开始接收
            while (channel.isOpen() && overTimeCount<OVER_MAX){
                if (count!=3){
                    sendbuf.clear();
                    sendbuf.put(Command.UDPTranslate.resourceUpload);
                    sendbuf.flip();
                    channel.send(sendbuf, element.toAddress);
                    LOG.I("请求服务器上传."+ count);
                }

                buffer.clear();
                address = channel.receive(buffer);
                if (  address != null && address.equals(element.toAddress)){
                    buffer.flip();
                    if (buffer.limit() == 1){
                        LOG.I("收到上传响应: "+ (++count));
                    }else if (buffer.limit()>8){
                        //数据分析:
                        sendCount = buffer.getLong();
                       if (sendCount == (recvCount+1)){
                            //接收数据
                            position = buffer.getLong();
                           Future<Integer> ops = fileChannel.write(buffer,position);
                           while (!ops.isDone());
                           LOG.I(position+" - "+ ops.get());
                           if (ops.get()==0){
                               //传输结束

                               //判断文件MD5是否正确,不正确重新传输.
                               String md5 = MD5Util.getFileMD5String(element.downloadFileTemp.toFile());
                               LOG.I("下载完成 - 文件MD5:"+ md5 +" , 源MD5" +element.downloadFileMD5);
                               if (md5.equalsIgnoreCase(element.downloadFileMD5)){
                                   //跳出循环
                                   break;
                               }else{
                                   recvCount = -1;
                                   LOG.I("请求重传,数据异常.");
                               }
                           }
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

                }
                else{
                   waitTime();
                   overTimeCount++;
                }
            }
            closeFileChannel(fileChannel);
            boolean f =  overTimeCount<OVER_MAX && element.downloadFileTemp.toFile().renameTo(element.downloadFile.toFile());
            LOG.I("下载结果 : "+f);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
           closeFileChannel(fileChannel);

        }
        return super.translation();
    }

    long curPos = 0L;
    @Override
    public void completed(Integer integer, Void aVoid) {
        LOG.I(" position  == " + integer +" 已保存大小 :" + (curPos+=integer) );
    }

    @Override
    public void failed(Throwable throwable, Void aVoid) {
        throwable.printStackTrace();
    }
}
