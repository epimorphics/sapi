package com.epimorphics.simpleAPI.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;

import com.epimorphics.appbase.tasks.ActionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;

@MXBean
public class ResponseMetrics implements ResponseMetricsMXBean {
    static Logger log = LoggerFactory.getLogger(ResponseMetrics.class);
    
    protected int succeeded = 0;
    protected int failed = 0;

    @Override
    public int getResponseSucceeded() {
        return succeeded;
    }

    @Override
    public void setResponseSucceeded(int succeeded) {
        this.succeeded = succeeded;
    }

    @Override
    public void incResponseSucceeded() {
        this.succeeded++;
    }

    @Override
    public int getResponseFailed() {
        return this.failed;
    }

    @Override
    public void setResponseFailed(int failed) {
        this.failed = failed;
    }

    @Override
    public void incResponseFailed() {
        this.failed++;
    }

    private static ResponseMetrics theInstance;

    public static ResponseMetrics getInstance() {
        if (theInstance == null) {
            theInstance = new ResponseMetrics();
            try {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                ObjectName mxbeanName = new ObjectName("com.epimorphics:type=ResponseMetrics,name=Response");
                mbs.registerMBean(theInstance, mxbeanName);
            } catch (MalformedObjectNameException | InstanceAlreadyExistsException |
                     MBeanRegistrationException | NotCompliantMBeanException e) {
                log.error("Failed to register response metric bean", e);
            }
        }
        return theInstance;
    }
}
