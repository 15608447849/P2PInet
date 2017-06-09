import client.obj.Info;
import client.obj.SerializeSource;
import client.socketimp.SocketManager;
import client.sourceimp.SourceManager;
import protocol.Parse;
import server.obj.IParameter;
import server.imp.threads.P2POperate;
import server.imp.servers.P2PServer;
import utils.LOG;
import utils.NetworkUtil;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Random;

public class Main {

    public static final String TCPserverIp = "125.65.82.99";
    public static final int TPO1 = 9999;
    public static final int TPO2 = 8888;


    public static void main(String[] args) {
//            test();
//            launchServer();
//        launchUdpServer();
            launchClient();
//            startSource("/psb.jpg");
    }




    /**
     * 启动服务器
     */
    private static void launchServer(){
        try {
        //启动TCP服务器
        IParameter info = new IParameter(NetworkUtil.getLocalIPInet(),TPO1,TPO2);
        P2POperate operate = new P2POperate(5);
        P2PServer server = new P2PServer();
        server.initServer(info);
        server.connectServer(operate);//连接操作
        server.createUdpManager(5000,6000);
        server.startServer();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 启动udp 辅助服务器
     */
    private static void launchUdpServer(){
        try {
            int port =  new Random().nextInt(10000)%(65535-10000+1) + 10000;
            InetSocketAddress localUdpAddress = new InetSocketAddress(NetworkUtil.getLocalIPInet(),port);
            InetSocketAddress remoteUdpAddress = new InetSocketAddress(TCPserverIp,TPO2);
            IParameter info = new IParameter(localUdpAddress,remoteUdpAddress);
            P2PServer server = new P2PServer();
            server.initServer(info);
            server.startServer();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 启动客户端
     */
    private static void launchClient() {
        try {
            /**
             * 写进配置文件中,读取.
             */
            String homeDirs = "C:\\FileServerDirs\\temp";
            File dir = new File(homeDirs);
            if (!dir.exists()){
                dir.mkdir();
            }
            int port =  new Random().nextInt(10000)%(65535-10000+1) + 10000;
            InetSocketAddress local = new InetSocketAddress(NetworkUtil.getLocalIPInet(),port);
//            InetSocketAddress server = new InetSocketAddress("172.16.0.198",9999);
            InetSocketAddress server = new InetSocketAddress(TCPserverIp,9999);
            Info info = new Info(local,server);
            SourceManager sourceManager = new SourceManager(homeDirs);
            new SocketManager(info,sourceManager).connectServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通知其他客户端本地下载了一个文件
     * @param savePath
     */
    private static void startSource(String savePath) {

        try {
            /**
             * 写进配置文件中,读取.
             */
            String homeDirs = "C:\\FileServerDirs\\source";
            int port =  new Random().nextInt(10000)%(65535-10000+1) + 10000;
            InetSocketAddress local = new InetSocketAddress(NetworkUtil.getLocalIPInet(),port);
            InetSocketAddress server = new InetSocketAddress(TCPserverIp,9999);
            Info info = new Info(local,server);
            SourceManager sourceManager = new SourceManager(homeDirs);
           SocketManager manager = new SocketManager(info,sourceManager);
            manager.connectServer();
            new Thread(() -> {
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    SerializeSource source = new SerializeSource(new File(homeDirs+savePath),savePath);
                    manager.commander.synchronizationSource(source);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }











    private static void test() {
        InetAddress address =  NetworkUtil.getLocalIPInet();
        InetSocketAddress socket = new InetSocketAddress(address,9999);
        LOG.I( socket.getHostName()+" "+ socket.getHostString()+" "+socket.getAddress().getAddress().length+" "+socket.getPort());

        try {
            InetAddress address1 = InetAddress.getByAddress(socket.getAddress().getAddress());
            LOG.I( address1+" ");
            LOG.I( "mac length: "+ NetworkUtil.getMACAddress(address1).length);
            LOG.I("主机名:"+address.getHostName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void test2(){
        byte[] bytes = Parse.int2bytes(62500);
        LOG.I( "int -> byte[] : "+ Arrays.toString(bytes) +" length:"+bytes.length);
        int i = Parse.bytes2int(bytes);
        LOG.I( "byte[] -> int : "+ i);
    }





}
