package server.abs;

import server.obj.CLI;

/**
 * Created by user on 2017/5/27.
 */
public interface IOperate {
    void setServer(IServer server);
    IServer getServer();
    void putCLI(CLI client);
    void turnSynchronizationSource(byte[] macBytes, byte[] sourceName);
    CLI getClientByMac(String mac);
}
