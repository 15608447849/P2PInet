package server.abs;

/**
 * Created by user on 2017/5/31.
 */
public abstract class IThread extends Thread  {
    /**
     * 是否运行
     */
    private volatile boolean isRun;
    /**
     * 服务器
     */
    public IServer server;

    public IThread(IServer server) {
        this.server = server;
    }

    /**
     * 启动
     */
    public void launch(){
        isRun = true;
        start();
    }

    /**
     * 终止
     */
    public void termination(){
        isRun = false;
        interrupt();
    }

    @Override
    public void run() {
        if (isRun){
            action();
        }
    }

    protected abstract void action();



}
