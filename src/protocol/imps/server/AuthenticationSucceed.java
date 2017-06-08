package protocol.imps.server;

import protocol.Excute;
import protocol.Intent;
import utils.LOG;

/**
 * Created by user on 2017/6/8.
 * 客户端认证成功
 */
public class AuthenticationSucceed implements Excute.IAction {
    @Override
    public void action(Intent intent) {
            intent.getServerCLI().setAuthentication(2);
            //认证成功
            LOG.I( intent.getServerCLI()+" net 认证完成. ");
    }
}
