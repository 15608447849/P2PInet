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
import java.util.concurrent.TimeUnit;

/**
 * Created by user on 2017/6/13.
 */
public class DataUpload extends DataImp{
    public DataUpload(DataElement element) {
        super(element);
    }

    @Override
    protected boolean waitLoaderNotify() {
        overTimeCount = OVER_INIT;
        LOG.I("等待上传命令中...");
        while (overTimeCount< OVER_MAX){
            try {
            element.buf2.clear();
            SocketAddress address  = element.channel.receive(element.buf2);
//                LOG.I("收到数据 - ."+address);
            if (address != null){
                 element.buf2.flip();
                cmd = element.buf2.get(0);
                if ( cmd == Command.UDPTranslate.resourceUpload){
                    LOG.I("收到数据上传命令.");
                    return true;
                }
            }else{
                sleep(overTime);
            }

            } catch (Exception e) {
            }
            overTimeCount++;
        }
        return super.waitLoaderNotify();
    }

    @Override
    protected boolean translateUp() {

        LOG.I("上传数据中.");
        overTimeCount = OVER_INIT;
        position = 0L;
        sendCount=0L;

        ByteBuffer sendbuf = element.buf1;
        ByteBuffer recvbuf = element.buf2;
        DatagramChannel channel = element.channel;

        //获取文件 管道
        AsynchronousFileChannel fileChannel = null;
        Future<Integer> ops = null;
        long readLength = 0L;
        long fileSzie = element.uploadFilePath.toFile().length();
        try {
            fileChannel = AsynchronousFileChannel.open(element.uploadFilePath, StandardOpenOption.READ);
            //开始写入

            while (channel.isOpen() && channel.isConnected() && overTimeCount<OVER_MAX){

                if (overTimeCount==OVER_INIT){ //读取本地文件
                    sendbuf.clear();
                    sendbuf.putLong(sendCount); //次数 -1数据流结束
                    if (sendCount!= -1L){
                        sendbuf.putLong(position);//下标
                        //数据
                        ops = fileChannel.read(sendbuf,position);
                        while (!ops.isDone());
                        try {
                            readLength = ops.get();
                        } catch (Exception e) {
                            e.printStackTrace();
                            readLength = 0L;
                        }
                        LOG.I("次数: "+ sendCount+" position == "+ position);
                        if (readLength!=0){
                            position+=readLength;
                        }
                    }
                    sendCount++;
                    sendbuf.flip();
                }else{
                    sendbuf.rewind();
                }

                //写入
                channel.send(sendbuf,element.toAddress);

                //接收回执
                recvbuf.clear();
                if (channel.receive(recvbuf)!=null){
                    recvbuf.flip();
                    if (recvbuf.limit()== 8 && recvbuf.getLong() == sendCount){
                        if (fileSzie == position){
                            sendCount= -1L; //退出 - 通知下载,传输完毕,   如果对方不需要重传,则超时等待结束. 如果对方需要重传, 传送 sendCount++; 自动重传.
                            position = 0L;
                        }else{
                            overTimeCount=OVER_INIT;//继续
                        }
                    }

                }else{
                    try {
                        TimeUnit.MICROSECONDS.sleep(overTime * 100);
                    } catch (InterruptedException e) {
                    }
//                    overTimeCount++;
                    if (overTimeCount==OVER_MAX) return false;
                }
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
           closeFileChannel(fileChannel);
        }

        return super.translateUp();
    }
}
