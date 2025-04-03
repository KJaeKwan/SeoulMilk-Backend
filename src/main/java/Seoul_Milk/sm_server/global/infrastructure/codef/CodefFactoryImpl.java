package Seoul_Milk.sm_server.global.infrastructure.codef;

import io.codef.api.EasyCodef;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CodefFactoryImpl implements CodefFactory{
    private final String PUBLIC_KEY;
    private final String CLIENT_ID;
    private final String CLIENT_SECRET;

    public CodefFactoryImpl(@Value("${codef.public.key}") String PUBLIC_KEY,
            @Value("${codef.client.id}") String CLIENT_ID,
            @Value("${codef.client.secret}") String CLIENT_SECRET){
        this.PUBLIC_KEY = PUBLIC_KEY;
        this.CLIENT_ID = CLIENT_ID;
        this.CLIENT_SECRET = CLIENT_SECRET;
    }
    public EasyCodef create(){
        EasyCodef easyCodef = new EasyCodef();
        easyCodef.setClientInfoForDemo(CLIENT_ID, CLIENT_SECRET);
        easyCodef.setPublicKey(PUBLIC_KEY);
        return easyCodef;
    }
}
