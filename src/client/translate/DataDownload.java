package client.translate;

import protocol.Command;
import protocol.Parse;
import utils.LOG;
import utils.MD5Util;

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
 */
public class DataDownload extends DataImp{
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
        SocketAddress address;

        int mtuValue = Parse.DATA_BUFFER_MAX_ZONE;
        ByteBuffer checkBuffer = ByteBuffer.allocate(mtuValue);
        try{
            fileChannel = AsynchronousFileChannel.open(element.downloadFileTemp, StandardOpenOption.WRITE);

            //开始接收
            while (channel.isOpen() && overTimeCount<OVER_MAX){
                if (checkBuffer!=null && mtuValue>Parse.UDP_DATA_MIN_BUFFER_ZONE){
                    checkBuffer.clear();
                    for (int i = 0;i<mtuValue;i++){
                        checkBuffer.put(Command.UDPTranslate.mtuCheck);
                    }
                    sendbuf.flip();
                    channel.send(checkBuffer, element.toAddress);
                    LOG.I("发送 MTU -> "+ mtuValue);
                    mtuValue--;
                    continue;
                }

                buffer.clear();
                address = channel.receive(buffer);
                if (  address != null && address.equals(element.toAddress)){
                    buffer.flip();
                    cmd = buffer.get(0);
                    if (cmd == Command.UDPTranslate.mtuCheck){
                       mtuValue =buffer.limit();
                        LOG.I("收到MTU响应,设置缓冲区 MTU : "+mtuValue);
                        checkBuffer.clear();
                        checkBuffer.put(Command.UDPTranslate.mtuSure);
                        checkBuffer.putInt(mtuValue);
                        checkBuffer.flip();
                        channel.send(checkBuffer,element.toAddress);
                        checkBuffer = null;
                        sendbuf = ByteBuffer.allocate(mtuValue);
                    }else if (buffer.remaining()>8){
                        buffer.rewind();
                        //数据分析:
                        sendCount = buffer.getLong();
                       if (sendCount == recvCount){
                            //接收数据
                            position = buffer.getLong();
                           Future<Integer> ops = fileChannel.write(buffer,position);
                           while (!ops.isDone());
                           //LOG.I(sendCount+" --> "+ position+" ---> "+ ops.get());
                           if (ops.get()==0){
                               //传输结束
                               //判断文件MD5是否正确,不正确重新传输.
                               String md5 = MD5Util.getFileMD5String(element.downloadFileTemp.toFile());
                               if (md5.equalsIgnoreCase(element.downloadFileMD5)){
                                   //跳出循环
                                   buffer.clear();
                                   closeFileChannel(fileChannel);
                                   LOG.I("下载完成 - 文件MD5:"+ md5 +" , 源MD5" +element.downloadFileMD5+" ,从命名:"+element.downloadFileTemp.toFile().renameTo(element.downloadFile.toFile()));
                                   return true;
                               }else{
                                   recvCount = -1;
                                   LOG.I("请求重传,数据异常.");
                               }
                           }
                               //回执
                               sendbuf.clear();
                               recvCount++;
                               sendbuf.putLong(recvCount);
                               sendbuf.flip();
                               channel.send(sendbuf,element.toAddress);
                               overTimeCount=OVER_INIT;
                        }
                    }
                }
                else{
                   waitTime();
                   overTimeCount++;
                }
            }



        }catch (Exception e){
            e.printStackTrace();
        }finally {
           closeFileChannel(fileChannel);

        }
        return super.translation();
    }

    long curPos = 0L;

}
