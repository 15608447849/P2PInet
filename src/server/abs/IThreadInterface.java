package server.abs;

import client.obj.SerializeConnectTask;
import server.imp.threads.UDPTemporary;

/**
 * Created by user on 2017/6/6.
 */
public interface IThreadInterface {
    void setPort(int start,int end);
    void putNewTask(SerializeConnectTask udpTemporary);
    void putUseConnect(int port, UDPTemporary udpTemporary);
    void removePort(int port);
}
