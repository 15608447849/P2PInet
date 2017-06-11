package client.Threads;

import client.sourceimp.SourceManager;
import protocol.Command;
import utils.LOG;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

/**
 * Created by user on 2017/6/6.
 * 客户端 文件下载
 */
public class TClientLoad extends TranslateThread {
    public TClientLoad(Translate translate) {
        super(translate);
        start();
    }

    @Override
    protected void openChannel() throws Exception {
        super.openChannel();
        LOG.I(TAG+"打开UDP管道.");
    }

    @Override
    protected void sendMessageToServer() throws Exception {
        LOG.I(TAG+"发送心跳到服务器.");
        super.sendMessageToServer();
    }




    @Override
    protected void sendMessageToTerminal() throws Exception {
        LOG.I(TAG+"终端信息: "+ translate.getTerminalSocket());
        super.sendMessageToTerminal();
    }

    @Override
    void translateData() throws Exception {
        LOG.I(TAG+ "数据下载....");
            //创建临时文件
        Path dirPath = translate.getSourceManager().getHome();
        String temFilePath = translate.getResource().getPosition()+".tmp";
        File temp = new File(dirPath+temFilePath);
        if (!temp.exists()){
            temp.createNewFile();
        }
        //通知数据发送
        ByteBuffer buffer = translate.getBuffer();
        buffer.clear();
        buffer.put(Command.UDPTranslate.resourceUpload);
        buffer.flip();
        translate.sendMessageToTarget(buffer,translate.getTerminalSocket(),translate.getChannel());
        FileChannel outChannel = new RandomAccessFile(temp,"rw").getChannel();

        //循环接受
        long length = 0;
        long fileLength = translate.getResource().getFileLength();
        while (length<fileLength){
            buffer.clear();
            SocketAddress socketAddress = translate.getChannel().receive(buffer);
            if (socketAddress!=null){
                buffer.flip();
                outChannel.write(buffer);
                length+=buffer.limit();
                LOG.I("当前进度:"+length);
            }
        }
        outChannel.close();
    }



}
