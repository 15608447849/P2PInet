package protocol;

import client.socketimp.SocketManager;
import server.abs.IOperate;
import server.obj.CLI;
import server.obj.IParameter;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.HashMap;

/**
 * Created by user on 2017/6/1.
 * 数据传递
 */
public class Intent {
    private byte command;
    private HashMap<String,Object> map;
    private IOperate operate;
    private IParameter param;
    private CLI serverCli;
    public Intent(){}
    public Intent(IOperate operate) {
        putOperate(operate);
    }
    public Intent(IOperate operate, IParameter param) {
        putOperate(operate);
        putIparam(param);
    }
    public void putIparam(IParameter param){
        this.param = param;
    }
    public IParameter getIparam(){
        return param;
    }

    public void putMap(HashMap<String,Object> map) {
        this.map = map;
    }

    public void putOperate(IOperate operate) {
        this.operate = operate;
    }

    public void putCLI(CLI serverCli) {
        this.serverCli= serverCli;
    }

    public HashMap<String,Object> getMap() {
        return map;
    }

    public CLI getServerCLI() {
        return serverCli;
    }

    public IOperate getOperate() {
        return operate;
    }

    public void putCommand(byte command) {
        this.command = command;
    }

    public byte getCommand() {
        return command;
    }










    private SocketManager socketManager;

    public SocketManager getSourceManager() {
        return socketManager;
    }

    public void putSocketManager(SocketManager socketManager) {
        this.socketManager = socketManager;
    }


}
