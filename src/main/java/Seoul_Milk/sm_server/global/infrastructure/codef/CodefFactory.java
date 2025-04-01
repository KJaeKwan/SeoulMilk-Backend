package Seoul_Milk.sm_server.global.infrastructure.codef;

import io.codef.api.EasyCodef;
import org.reactivestreams.Publisher;

public interface CodefFactory {
    EasyCodef create();
}
