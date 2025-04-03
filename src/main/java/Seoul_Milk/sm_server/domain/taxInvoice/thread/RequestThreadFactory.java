package Seoul_Milk.sm_server.domain.taxInvoice.thread;

import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.global.infrastructure.redis.RedisUtils;
import io.codef.api.EasyCodef;
import java.util.HashMap;

public interface RequestThreadFactory {
    Thread create(String id, EasyCodef easyCodef, HashMap<String, Object> requestData, int index,
            String url, RedisUtils redisUtils, TaxInvoiceRepository taxInvoiceRepository, String approvalNo);
}
