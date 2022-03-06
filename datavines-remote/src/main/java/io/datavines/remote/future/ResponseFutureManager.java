package io.datavines.remote.future;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zixi0825
 */
public class ResponseFutureManager {

    private static final Logger logger = LoggerFactory.getLogger(ResponseFutureManager.class);

    private static final ConcurrentHashMap<Long,ResponseFuture> FUTURE_TABLE = new ConcurrentHashMap<>(256);

    public static void putResponseFuture(long opaque,ResponseFuture responseFuture){
        FUTURE_TABLE.put(opaque,responseFuture);
    }

    public static void removeResponseFuture(long opaque){
        FUTURE_TABLE.remove(opaque);
    }

    public static ResponseFuture getResponseFuture(long opaque){
        return FUTURE_TABLE.get(opaque);
    }

    /**
     * scan future table
     */
    public static void scanFutureTable() {
        final List<ResponseFuture> futureList = new LinkedList<>();
        Iterator<Map.Entry<Long, ResponseFuture>> it = FUTURE_TABLE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, ResponseFuture> next = it.next();
            ResponseFuture future = next.getValue();
            if ((future.getBeginTimestamp() + future.getTimeoutMillis() + 1000) <= System.currentTimeMillis()) {
                futureList.add(future);
                it.remove();
                logger.warn("remove timeout request : {}", future);
            }
        }

        for (ResponseFuture future : futureList) {
            try {
                future.release();
                future.executeInvokeCallback();
            } catch (Throwable ex) {
                logger.warn("scanFutureTable, execute callback error", ex);
            }
        }
    }
}
