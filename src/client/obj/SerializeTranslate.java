package client.obj;

import client.obj.SerializeConnectTask;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/6/10.
 */
public class SerializeTranslate implements Serializable {
    //对方的NAT
    public InetSocketAddress address;
    //自己的模式
    public int mode;

    public SerializeTranslate(InetSocketAddress address, int mode) {
        this.address = address;
        this.mode = mode;
    }

}
