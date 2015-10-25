/******************************************************************
 * File:        TestRequestBasics.java
 * Created by:  Dave Reynolds
 * Created on:  2 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.jena.rdf.model.Literal;
import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.query.DataSource;
import com.epimorphics.simpleAPI.results.ResultStream;
import com.epimorphics.simpleAPI.results.TreeResult;

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
    
    @Test 
    public void testSort() {
        assertEquals(10, getAndCount("listTest2"));
        assertEquals("A1", getFirstLabel("listTest2", "_sort", "label"));
        assertEquals("B5", getFirstLabel("listTest2", "_sort", "-label"));
        assertEquals("A5", getFirstLabel("listTest2", "_sort", "group", "_sort", "-label"));
        assertEquals("B1", getFirstLabel("listTest2", "_sort", "narrower.label"));
    }
    
    @Test
    public void testFilter() {
        assertEquals("B1", getFirstLabel("listTest2", "group", "B", "_sort", "label"));
        assertEquals("B1", getFirstLabel("listTest2", "narrower.group", "A", "_sort", "label"));
        assertEquals("B2", getFirstLabel("listTest2", "group", "B", "notation", "2"));
        assertEquals("B2", getFirstLabel("listTest2", "group", "B", "narrower.notation", "2"));
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
        Call call = new Call(api, endpointName, request);
        return (ResultStream)call.getResults();
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
    
    private String getFirstLabel(String endpointName, String... args) {
        ResultStream stream = get(endpointName, makeRequest(args));
        assertTrue( stream.iterator().hasNext() );
        TreeResult first = (TreeResult) stream.iterator().next();
        Literal label = (Literal) first.getValues("label").iterator().next();
        return label.getLexicalForm();
    }
}
