package server.obj;

import client.obj.SerializeConnectTask;
import protocol.Command;
import protocol.Parse;
import utils.LOG;
import utils.NetworkUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by user on 2017/6/2.
 */
public class CLIWrite {
    private CLI client;
    public CLIWrite(CLI client) {
        this.client = client;
    }

    /**
     * 通知认证NET类型
     */
    public void notifyAuthentication(byte[] udp1portBytes) {
        if (client.isValid()){
            ByteBuffer buff = ByteBuffer.allocate(1+4+4);
            buff.put(Command.Server.authenticationNetType);//命令
            buff.put(Parse.int2bytes(4));
            buff.put(udp1portBytes);
            buff.flip();
            client.getSocket().write(buff);
        }
    }

    /**
     * 资源同步下发
     * {turn,长度,"对方mac地址,资源bytes"}
     * -如果有结果返回 : 对方mac地址,本地文件的文件全路径
     */
    public void synchronizationSourceIssued(byte[] hostMacBytes,byte[] source) {
        if (client.getMac() != null
                &&  !NetworkUtil.macByte2String(hostMacBytes).equals(client.getMac())
                && client.isValid()){
                int len = source.length;
                LOG.E("资源同步对象序列化长度:"+len);
                byte[] lengthBytes = Parse.int2bytes(len);
                ByteBuffer buff = ByteBuffer.allocate(1+lengthBytes.length+len);
                buff.put(Command.Server.turnSynchronizationSource);//命令
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
                byte[] data = Parse.sobj2Bytes(connectTask);
                int len = data.length;
                LOG.E("传输对象序列化长度:"+len);
                byte[] lenBytes =  Parse.int2bytes(len);
                ByteBuffer buff = ByteBuffer.allocate(1+lenBytes.length+len);
                buff.put(cmd);//命令
                buff.put(lenBytes);//长度
                buff.put(data);//资源信息序列化对象
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
