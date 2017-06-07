package client.Threads;

import client.obj.SerializeConnectTask;
import protocol.Command;
import protocol.Parse;
import utils.LOG;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;

/**
 * Created by user on 2017/6/6.
 */
public class ClientA extends TanslateThread {
    private static final String TAG = "客户端A #";
    public ClientA(Translate translate) {
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
    boolean onServerMessage(ByteBuffer byteBuffer) {
        try {
            byte resultCommand = byteBuffer.get(0);
            if (resultCommand == Command.Server.udpSourceDestNetAddress){

                //获取数据
                int len = byteBuffer.getInt(1);
                byte[] data = new byte[len];
                byteBuffer.position(5);
                byteBuffer.get(data,0,len);

                SerializeConnectTask connectTask = (SerializeConnectTask) Parse.bytes2Sobj(data);
                if(connectTask.getCompele() == 3){
                    tanslate.setTerminalSocket(connectTask.getDesNet());
                    LOG.I(TAG+"收到客户端B的地址信息.");
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    void sendMessageToTerminal() throws Exception {
        LOG.I(TAG+"终端信息: "+ tanslate.getTerminalSocket());
        ByteBuffer buffer = tanslate.getBuffer();
        while (true){
            buffer.clear();
            buffer.put(Command.Client.clientAshakePackage);
            buffer.flip();
            tanslate.sendMessageToTarget(buffer,tanslate.getTerminalSocket(),tanslate.getChannel());

            buffer.clear();
            InetSocketAddress terminal = (InetSocketAddress) tanslate.getChannel().receive(buffer);
            if (terminal!=null && terminal.equals(tanslate.getTerminalSocket())){
                buffer.flip();
                LOG.I(TAG+"收到对端信息, "+ buffer.get(0));
                break;
            }
        }
    }

    @Override
    void tanslateData() throws Exception {

    }

    @Override
    void closeChannel() throws Exception {

    }






















}
