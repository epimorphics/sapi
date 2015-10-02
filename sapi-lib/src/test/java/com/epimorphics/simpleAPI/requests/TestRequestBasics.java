/******************************************************************
 * File:        TestRequestBasics.java
 * Created by:  Dave Reynolds
 * Created on:  2 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.epimorphics.appbase.core.App;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.DataSource;
import com.epimorphics.simpleAPI.query.Query;
import com.epimorphics.simpleAPI.results.ResultStream;

public class TestRequestBasics {
    App app;
    API api;
    DataSource source;
    
    @Before
    public void setUP() throws IOException {
        app = new App("test", new File("src/test/baseRequestTest/app.conf"));
        api = app.getA(API.class);
        app.startup();
        source = app.getA(DataSource.class);
    }

    @Test
    public void testLimit() {
        assertEquals(4, getAndCount("listTest1"));
        assertEquals(5, getAndCount("listTest1", "_limit", "5"));
        assertEquals(8, getAndCount("listTest1", "_limit", "10"));
        assertEquals(5, getAndCount("listTest1", "_limit", "5", "_offset", "2"));
        assertEquals(4, getAndCount("listTest1", "_limit", "5", "_offset", "6"));
    }
    
    private Request makeRequest(String... args) {
        Request request = new Request();
        for (int i = 0; i < args.length;) {
            String p = args[i++];
            String v = args[i++];
            request.add(p,v);
        }
        return request;
    }
    
    private ResultStream get(String endpointName, Request request) {
        EndpointSpec spec = api.getSpec(endpointName);
        Query query = spec.getQueryBuilder(request).build();
        return source.query(query, spec);
    }
    
    private int getAndCount(String endpointName, String... args) {
        return getAndCount(endpointName, makeRequest(args));
    }
        
    private int getAndCount(String endpointName, Request request) {
        ResultStream stream = get(endpointName, request);
        int count = 0;
        while (stream.hasNext()) {
            stream.next();
            count++;
        }
        return count;
    }
}
