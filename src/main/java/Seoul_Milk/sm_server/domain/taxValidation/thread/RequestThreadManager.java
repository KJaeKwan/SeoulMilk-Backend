package Seoul_Milk.sm_server.domain.taxValidation.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestThreadManager {
    private static final Map<String, List<RequestThread>> runningThreads = new HashMap<>();

    public static synchronized void addThread(String key, RequestThread thread) {
        runningThreads.computeIfAbsent(key, k->new ArrayList<>()).add(thread);
    }

    public static synchronized void waitForThreads(String key) throws InterruptedException {
        List<RequestThread> threads = runningThreads.get(key);
        if(threads != null){
            for(Thread thread : threads){
                thread.join();
            }
            runningThreads.remove(key);
        }
    }
}
