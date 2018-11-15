package com.github.onsdigital.brian.filter;

import org.slf4j.MDC;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.utils.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.github.onsdigital.brian.filter.FilterKeys.REQUEST_ID_HEADER;
import static com.github.onsdigital.brian.filter.FilterKeys.REQUEST_RECIEVED_KEY;
import static com.github.onsdigital.brian.filter.FilterKeys.REQ_METHOD_KEY;
import static com.github.onsdigital.brian.filter.FilterKeys.REQ_RECEIVED_MSG;
import static com.github.onsdigital.brian.logging.LogEvent.logEvent;

/**
 * A Filter to pass requests through before invoking the target {@link spark.Route#handle(Request, Response)} method.
 * Populates the request ID if its does not exist, Captures the time the request was received (used later in metrics
 * logging) and logs other useful debugging information.
 */
public class BeforeHandleFilter implements Filter {

    @Override
    public void handle(Request request, Response response) throws Exception {
        captureRequestStartTime();
        addRequestID(request);

        logEvent().uri(request.uri())
                .parameter(REQ_METHOD_KEY, request.requestMethod())
                .info(REQ_RECEIVED_MSG);
    }

    private void addRequestID(Request request) {
        String requestID = request.headers(REQUEST_ID_HEADER);
        if (StringUtils.isEmpty(requestID)) {
            requestID = UUID.randomUUID().toString();
        }
        MDC.put(REQUEST_ID_HEADER, requestID);
    }

    private void captureRequestStartTime() {
        MDC.put(REQUEST_RECIEVED_KEY, LocalDateTime.now().toString());
    }
}
