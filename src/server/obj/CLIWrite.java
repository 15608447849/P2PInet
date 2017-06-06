package server.obj;

import client.obj.SerializeConnectTask;
import protocol.Command;
import protocol.Parse;
import utils.LOG;
import utils.NetUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by user on 2017/6/2.
 */
public class CLIWrite {
    private ServerCLI client;
    public CLIWrite(ServerCLI client) {
        this.client = client;
    }

    /**
     * 资源同步下发
     * {turn,长度,"对方mac地址,资源bytes"}
     * -如果有结果返回 : 对方mac地址,本地文件的文件全路径
     */
    public void synchronizationSourceIssued(byte[] hostMacBytes,byte[] source) {
        if (client.getMac() != null
                &&  !NetUtil.macByte2String(hostMacBytes).equals(client.getMac())
                && client.isValid()){
               int len = source.length;
               byte[] lengthBytes = Parse.int2bytes(len);
               ByteBuffer buff = ByteBuffer.allocate(1+lengthBytes.length+len);
                buff.put(Command.Server.trunSynchronizationSource);//命令
                buff.put(lengthBytes);//长度
                buff.put(source);//资源信息序列化对象
                buff.flip();
               client.getSocket().write(buff);
        }
    }

    /**
     * Command.Server.
     * queryConnectUdp_source
     * queryConnectUdp_der
     */
    public boolean notifyConnect(byte cmd,SerializeConnectTask connectTask) {
        if (client.isValid()){
            try {
                byte[] datas = Parse.sobj2Bytes(connectTask);
                int len = datas.length;
                byte[] lenBytes =  Parse.int2bytes(len);
                ByteBuffer buff = ByteBuffer.allocate(1+lenBytes.length+len);
                buff.put(cmd);//命令
                buff.put(lenBytes);//长度
                buff.put(datas);//资源信息序列化对象
                buff.flip();
                client.getSocket().write(buff);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;
    }
}
