package com.epimorphics.simpleAPI.metrics;

import javax.management.MXBean;

@MXBean
public interface ResponseMetricsMXBean {
    public int getResponseSucceeded();
    public void setResponseSucceeded(int succeeded);
    public void incResponseSucceeded();

    public int getResponseFailed();
    public void setResponseFailed(int failed);
    public void incResponseFailed();
}
