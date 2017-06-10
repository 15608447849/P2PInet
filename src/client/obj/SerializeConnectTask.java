package client.obj;

import utils.NetworkUtil;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by user on 2017/6/5.
 */
public class SerializeConnectTask implements Serializable {
    private byte[] downloadMac;//请求资源的主机地址
    private SerializeSource source;//请求得资源 ->包含资源发起者
    /**
     * 服务器udp临时端口 - 服务器完成
     */
    private InetSocketAddress serverTempAddress;

    /**
     * 上传主机NET信息 - 服务器填写
     */
   private InetSocketAddress uploadHostAddress;
    /**
     * 下载主机的NET信息 - 服务器填写
     */
        private InetSocketAddress downloadHostAddress;

    /**
     * 1 服务器临时端口完成
     * 3 设置了源,目的地.
     * 5 两边客户端都收到服务器的命令
     */
    private int complete = 0;



    public SerializeConnectTask( SerializeSource source) {
        this.source = source;
    }
    public void setDownloadMac(byte[] downloadMac){
        this.downloadMac = downloadMac;
    }
    public void setServerTempAddress(InetSocketAddress address){
        serverTempAddress = address;
        complete = 1;
    }
    public InetSocketAddress getServerTempAddress(){
        return serverTempAddress;
    }
    //下载网关
    public void setDownloadHostAddress(InetSocketAddress address){
        if (complete<3 && downloadHostAddress == null){
            downloadHostAddress = address;
            complete++;
        }

    }
    //下载
    public InetSocketAddress getDownloadHostAddress() throws UnknownHostException {
        return downloadHostAddress;
    }

    public void setComplete(int i){
        complete = i;
    }
    public int getComplete(){
        return complete;
    }


    //上传
    public void setUploadHostAddress(InetSocketAddress address){
        if (complete<3 && uploadHostAddress == null){
           uploadHostAddress = address;
            complete++;
        }
    }
    public InetSocketAddress getUploadHostAddress() {
        return uploadHostAddress;
    }
    public String getUploadHostMac(){
        return NetworkUtil.macByte2String(getSource().getUploaderMac());
    }
    public SerializeSource getSource(){
        return source;
    }
    public String getDownloadHostMac() {
        return NetworkUtil.macByte2String(downloadMac);
    }

}
