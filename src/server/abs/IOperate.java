package server.abs;

import client.obj.SerializeConnectTask;
import server.obj.ServerCLI;

/**
 * Created by user on 2017/5/27.
 */
public interface IOperate {
    void setServer(IServer server);
    IServer getServer();
    void putCLI(ServerCLI client);
    void turnSynchronizationSource(byte[] macBytes, byte[] sourceName);
    ServerCLI getClientByMac(String mac);
}
