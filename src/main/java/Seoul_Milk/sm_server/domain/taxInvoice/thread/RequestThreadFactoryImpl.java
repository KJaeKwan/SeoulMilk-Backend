package Seoul_Milk.sm_server.domain.taxInvoice.thread;

import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.global.infrastructure.redis.RedisUtils;
import io.codef.api.EasyCodef;
import java.util.HashMap;
import org.springframework.stereotype.Component;

@Component
public class RequestThreadFactoryImpl implements RequestThreadFactory{

    @Override
    public Thread create(String id, EasyCodef easyCodef, HashMap<String, Object> requestData, int index, String url,
            RedisUtils redisUtils, TaxInvoiceRepository taxInvoiceRepository, String approvalNo) {
        return new RequestThread(id, easyCodef, requestData, index, url, redisUtils, taxInvoiceRepository, approvalNo);
    }
}
