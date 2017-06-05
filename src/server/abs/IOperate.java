package server.abs;

import server.obj.ServerCLI;

/**
 * Created by user on 2017/5/27.
 */
public interface IOperate {
    void putCLI(ServerCLI client);
    void turnSynchronizationSource(byte[] macBytes, byte[] sourceName);
}
