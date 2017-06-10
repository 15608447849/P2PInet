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
     * 资源上传
     */
    private byte[] uploaderMac;
    /**
     * 相对路径
     */
    public String position;
    private String fileName;
    private long fileLength;
    private String md5Hash;

    public SerializeSource(){}

    public SerializeSource(File file, String position) throws IOException {
        if (file == null) throw new NullPointerException("file is null.");
        this.fileName = file.getName();
        this.fileLength = file.length();
        this.position = position;
        this.md5Hash = MD5Util.getFileMD5String(file);
    }
    public String getPosition() {
        return position;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileLength() {
        return fileLength;
    }

    public String getMd5Hash() {
        return md5Hash;
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
        stringBuffer.append("上传主机: " + NetworkUtil.macByte2String(uploaderMac));
        stringBuffer.append(" 资源位置: "+position);
        stringBuffer.append(" ,资源名: "+fileName);
        stringBuffer.append(" ,资源大小: "+fileLength);
        stringBuffer.append(" ,资源MD5-hash: "+md5Hash);
        return stringBuffer.toString();
    }
}
