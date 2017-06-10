package protocol.imps.server;

import protocol.Execute;
import protocol.Intent;
import protocol.Parse;
import utils.LOG;

/**
 * Created by user on 2017/6/8.
 * 客户端认证成功
 */
public class AuthenticationSucceed implements Execute.IAction {
    @Override
    public void action(Intent intent) {
        try {

            //认证成功
            //更新mac地址
            byte[] macBytes = (byte[]) intent.getMap().get(Parse._macBytes);
            byte[] natTypeBytes = (byte[]) intent.getMap().get(Parse._natTypeBytes);
            //更新nat类型
            intent.getServerCLI().setMacAddress(macBytes);
            intent.getServerCLI().setNatType(Parse.bytes2int(natTypeBytes));
            intent.getServerCLI().setAuthentication(2);
            LOG.I( intent.getServerCLI()+" 客户端认证完成, NAT类型:  "+intent.getServerCLI().getNatType());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
