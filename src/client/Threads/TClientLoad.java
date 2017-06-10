package client.Threads;

import protocol.Command;
import utils.LOG;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

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
            LOG.I(TAG+ "数据传输....");
    }



}
