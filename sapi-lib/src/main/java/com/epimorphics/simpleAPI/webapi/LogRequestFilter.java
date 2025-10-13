/******************************************************************
 * File:        LogRequestFilter.java
 * Created by:  Dave Reynolds
 * Created on:  14 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.Status;

import com.epimorphics.simpleAPI.metrics.ResponseMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.util.NameUtils;
import org.slf4j.MDC;

/**
 * A Filter that can be added to filter chain to log all incoming requests and
 * the corresponding response (with response code and execution time). Assigns a 
 * simple request number to each request and includes that in the response headers
 * for diagnosis. Not robust against restarts but easier to work with than UUIDs.
 */
public class LogRequestFilter implements Filter {
    public static final String TRANSACTION_ATTRIBUTE = "transaction";
    public static final String START_TIME_ATTRIBUTE  = "startTime";
    public static final String RESPONSE_ID_HEADER = "x-response-id";
    public static final String REQUEST_ID_HEADER = "x-request-id";
    
    static final Logger log = LoggerFactory.getLogger( LogRequestFilter.class );
    
    protected static AtomicLong transactionCount = new AtomicLong(0);

  
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;
        String path = httpRequest.getRequestURI();
        String query = httpRequest.getQueryString();
        long transaction = transactionCount.incrementAndGet();
        String requestID = httpRequest.getHeader(REQUEST_ID_HEADER);
        long start = System.currentTimeMillis();

        MDC.put("method", ((HttpServletRequest) request).getMethod());
        MDC.put("path", path);
        if (requestID != null) {
            MDC.put("request_id", requestID);
        }
        MDC.put("request_status", "received");
        MDC.put("transaction_id", Long.toString(transaction));
        log.info( String.format("Request  [%d] : %s", transaction, path) + (query == null ? "" : ("?" + query)) );
        MDC.put("request_status", "processing");
        httpResponse.addHeader(RESPONSE_ID_HEADER, Long.toString(transaction));

        chain.doFilter(request, response);

        Long durationMS = System.currentTimeMillis() - start;
        int status = httpResponse.getStatus();
        boolean failed = status > 400 && status != 404;
        MDC.put("request_status", failed ? "error" : "completed");
        MDC.put("status", Integer.toString(status));
        updateMetrics(status);
        MDC.put("request_time", String.format("%.3f", durationMS/1000.0));
        if (failed) {
            log.error(String.format("Response [%d] : %d (%s)", transaction, status, NameUtils.formatDuration(durationMS)));
        } else {
            log.info( String.format("Response [%d] : %d (%s)", transaction, status, NameUtils.formatDuration(durationMS)));
        }
        MDC.clear();
    }

    private void updateMetrics(int status) {
        ResponseMetrics metrics = ResponseMetrics.getInstance();
        if (status > 400 && status != 404) {
            metrics.incResponseFailed();
        } else {
            metrics.incResponseSucceeded();
        }
    }

    @Override
    public void destroy() {
    }
    
    public static WebApiException badRequestException(String message) {
        log.warn("Bad request: " + message);
        return new WebApiException(Status.BAD_REQUEST, message);
    }
    
    public static WebApiException serverErrorException(String message) {
        log.warn("Server error: " + message);
        return new WebApiException(Status.INTERNAL_SERVER_ERROR, message);
    }

}
