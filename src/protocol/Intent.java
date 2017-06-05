package protocol;

import server.abs.IOperate;
import server.obj.ServerCLI;

import java.util.HashMap;

/**
 * Created by user on 2017/6/1.
 * 数据传递
 */
public class Intent {
    private HashMap<String,Object> map;
    private IOperate operate;
    private ServerCLI serverCli;
    public void putMap(HashMap<String,Object> map) {
        this.map = map;
    }

    public void putOperate(IOperate operate) {
        this.operate = operate;
    }

    public void putServerCLI(ServerCLI serverCli) {
        this.serverCli= serverCli;
    }

    public HashMap<String,Object> getMap() {
        return map;
    }

    public ServerCLI getServerCLI() {
        return serverCli;
    }

    public IOperate getOperate() {
        return operate;
    }
}
