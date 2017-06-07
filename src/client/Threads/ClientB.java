package client.Threads;

import protocol.Command;
import utils.LOG;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by user on 2017/6/6.
 */
public class ClientB extends TranslateThread {
    public ClientB(Translate translate) {
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
    boolean onServerMessage(InetSocketAddress socketAddress,ByteBuffer byteBuffer) {
        byte resultCommand = byteBuffer.get(0);
        if (resultCommand == Command.Server.udpServerReceiveHeartbeatSuccess){
                LOG.I(TAG+"收到服务器的心跳回执.");
                return false;
        }
        return true;
    }


    @Override
    protected void sendMessageToTerminal() throws Exception {
        LOG.I(TAG+"终端信息: "+ translate.getTerminalSocket());
        super.sendMessageToTerminal();
    }

    @Override
    void tanslateData() throws Exception {

    }

    @Override
    void closeChannel() throws Exception {

    }

}
