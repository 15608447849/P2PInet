package client.obj;

import utils.MD5Util;
import utils.NetworkUtil;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by lzp on 2017/6/5.
 *  如果position不存在->
 *  根据文件名 或者 文件长度相同, 对比MD5.
 *
 */
public class SerializeSource implements Serializable{
    /**
     * 资源上传主机
     */
    private byte[] uploaderMac;
    /**
     * 相对路径
     */
    private String position;
    private long size;
    private String md5;
    public SerializeSource(File file, String position) throws IOException {
        this.size = file.length();
        this.position = position;
        this.md5 = MD5Util.getFileMD5String(file);
    }
    public String getPosition() {
        return position;
    }
    public long getSize() {
        return size;
    }
    public String getMd5() {
        return md5;
    }
    public void setUploaderMac(byte[] uploaderMac) {
        this.uploaderMac = uploaderMac;
    }
    //资源上传者
    public byte[] getUploaderMac() {
        return uploaderMac;
    }
    @Override
    public String toString(){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("资源上传主机:" + NetworkUtil.macByte2String(uploaderMac))
        .append(",资源位置: "+position)
        .append(",资源大小: "+ size)
        .append(",资源MD5: "+ md5);
        return stringBuffer.toString();
    }
}
