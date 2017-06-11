package client.Threads;

import client.obj.SerializeConnectTask;
import protocol.Command;
import protocol.Parse;
import utils.LOG;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by user on 2017/6/6.
 *  客户端文件上传
 */
public class TClientUp extends TranslateThread {
    public TClientUp(Translate translate) {
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
        LOG.I(TAG+ "数据上传....");
        ByteBuffer buffer = translate.getBuffer();

        //等待开始上传命令
        while (true){
            buffer.clear();
            SocketAddress address = translate.getChannel().receive(buffer);
            if (address!=null){
                buffer.flip();
                if (buffer.get(0) == Command.UDPTranslate.resourceUpload){
                    break;
                }
            }
        }
        String filePath = translate.getSourceManager().getHome()+translate.getResource().getPosition();
        File file = new File(filePath);
        //开始上传数据
        FileChannel inChannel = new FileInputStream(file).getChannel();
        MappedByteBuffer fileBuffer = inChannel.map(FileChannel.MapMode.READ_ONLY,0,inChannel.position());
        long position = 0;
        long fileLength = inChannel.position();
        while (fileBuffer.hasRemaining()){
            buffer.clear();
            while (fileBuffer.hasRemaining()){
                buffer.put(fileBuffer.get());
                if (buffer.position() == buffer.limit()) break;
            }
            buffer.flip();
            translate.sendMessageToTarget(buffer,translate.getTerminalSocket(),translate.getChannel());
            synchronized (this){
                this.wait(20);
            }
        }
        //关闭
        fileBuffer = null;
        inChannel.close();
    }























}
