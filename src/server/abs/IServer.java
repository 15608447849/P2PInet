package server.abs;

import java.net.URLConnection;

/**
 * Created by user on 2017/5/27.
 */
public interface IServer {
    /**
     * 初始化服务参数
     * @param parameter
     */
    void initServer(IParameter parameter);

    /**
     * 设置服务操作
     * @param operate
     */
    void connectServer(IOperate operate);

    /**
     * 创建udp管理
     */
    void createUdpManager(int startPort,int endPort);
    /**
     * 开始服务
     */
    void startServer();

    /**
     * 停止服务
     */
    void stopServer();


    /**
     * 获取参数
     */
    Object getParam(String name);
}
