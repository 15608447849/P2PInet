package client.translate;

import protocol.Command;
import utils.LOG;

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
public class DataUpload extends DataImp{
    public DataUpload(DataElement element) {
        super(element);
    }

    @Override
    protected boolean waitingNotify() {
        try {
            overTimeCount = OVER_INIT;
            LOG.I("等待上传通知中.");
            int count = 0;
            while (overTimeCount< OVER_MAX){
                element.buf2.clear();
                SocketAddress address  = element.channel.receive(element.buf2);
    //                LOG.I("收到数据 - ."+address);
                if (address != null && address.equals(element.toAddress)){
                     element.buf2.flip();
                    cmd = element.buf2.get(0);
                    if ( cmd == Command.UDPTranslate.resourceUpload){
                        LOG.I("收到数据上传通知. - 记录地址:"+ address + " - client: "+ element.toAddress);
                        //响应
                        element.channel.send(element.buf2,address);
                        overTimeCount=OVER_INIT;
                        count++;
                        if (count==3) return true;
                    }
                }else{
                    waitTime();
                    overTimeCount++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.waitingNotify();
    }

    @Override
    protected boolean translation() {

        LOG.I("上传数据中.");
        overTimeCount = OVER_INIT;
        position = 0L;
        sendCount=0L;

        ByteBuffer sendbuf = element.buf1;
        ByteBuffer recvbuf = element.buf2;
        DatagramChannel channel = element.channel;
        long fileSize = element.uploadFilePath.toFile().length();

        //获取文件 管道
        AsynchronousFileChannel fileChannel = null;
        Future<Integer> ops ;
        long readLength ;
        SocketAddress address;
        try {
            fileChannel = AsynchronousFileChannel.open(element.uploadFilePath, StandardOpenOption.READ);
            //开始写入

            while (channel.isOpen() && overTimeCount<OVER_MAX){

                if (overTimeCount == OVER_INIT){ //读取本地文件
                    sendbuf.clear();
                    sendbuf.putLong(sendCount); //次数 -1数据流结束
                    sendbuf.putLong(position);//下标
                    //数据
                    ops = fileChannel.read(sendbuf,position);
                    while (!ops.isDone());
                    readLength = ops.get();
                        LOG.I("当前读取次数: "+ sendCount+" position:"+ position+" 当次读取量:"+readLength);
                        if (readLength > 0){
                            position+=readLength;
                            sendCount++;
                        }else if (readLength == -1){
                            //读取完毕
                            LOG.I("文件读取结束.");
                            //初始化值
                            sendCount = 0;
                            position = 0;
                        }
                    sendbuf.flip();
                }else{
                    sendbuf.rewind();
                }
                //写入

                int i = channel.send(sendbuf,element.toAddress);

                //接收回执
                recvbuf.clear();
                address = channel.receive(recvbuf);
                if (address!=null && address.equals(element.toAddress)){
                    recvbuf.flip();
                    if (recvbuf.limit()== 8 && recvbuf.getLong() == sendCount){
                            overTimeCount=OVER_INIT;//继续
                    }
                }else{
//                    LOG.I("超时 - "+overTimeCount);
                    waitTime();

//                    LOG.I("等待结束");
                    overTimeCount++;
                }
            }

            return sendCount==0&&position==0;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
           closeFileChannel(fileChannel);
        }

        return super.translation();
    }
}
