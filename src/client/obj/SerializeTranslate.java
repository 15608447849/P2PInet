package client.obj;

import client.obj.SerializeConnectTask;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/6/10.
 */
public class SerializeTranslate implements Serializable {
    public SerializeConnectTask connectTask;
    public int mode;
}
