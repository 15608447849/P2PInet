package server.abs;

/**
 * Created by user on 2017/5/27.
 *
 */
public abstract class IParameter {
    public String getMac() {
        return null;
    }

    public int getPort() {
        return -1;
    }

    public byte[] getIpBytes(){
        return null;
    }
}
