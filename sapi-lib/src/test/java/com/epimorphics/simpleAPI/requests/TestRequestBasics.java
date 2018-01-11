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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.rdf.model.Literal;
import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.query.DataSource;
import com.epimorphics.simpleAPI.query.impl.SparqlDataSource;
import com.epimorphics.simpleAPI.results.Result;
import com.epimorphics.simpleAPI.results.ResultStream;
import com.epimorphics.simpleAPI.results.TreeResult;
import com.epimorphics.simpleAPI.util.LastModified;
import com.epimorphics.util.TestUtil;

public class TestRequestBasics {
    App app;
    API api;
    DataSource source;
    
    @Before
    public void setUP() throws IOException {
        app = new App("test", new File("src/test/testCases/baseRequestTest/app.conf"));
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
        assertEquals("A5", getFirstLabel("listTest2", "_sort", "-narrower.label"));
    }
    
    @Test
    public void testFilter() {
        assertEquals("B1", getFirstLabel("listTest2", "group", "B", "_sort", "label"));
        assertEquals("B1", getFirstLabel("listTest2", "narrower.group", "A", "_sort", "label"));
        assertEquals("B2", getFirstLabel("listTest2", "group", "B", "notation", "2"));
        assertEquals("B2", getFirstLabel("listTest2", "group", "B", "narrower.notation", "2"));
    }
    
    @Test
    public void testExistsFilter() {
        TestUtil.testArray( getStringValues("list-exists-test", "notation", "_sort", "notation"), new String[]{"1", "2", "3"});
        TestUtil.testArray( getStringValues("list-exists-test", "notation", "exists-group", "true", "_sort", "notation"), new String[]{"1", "2"});
        TestUtil.testArray( getStringValues("list-exists-test", "notation", "exists-group", "false", "_sort", "notation"), new String[]{"3"});
    }
    
    @Test
    public void testRangeFilter() {
        TestUtil.testArray( getStringValues("listTest2", "notation", "group", "A", "minEx-notation", "2",  "_sort", "notation"), new String[] {"3", "4", "5"} );
        TestUtil.testArray( getStringValues("listTest2", "notation", "group", "A", "min-notation", "2",  "_sort", "notation"), new String[] {"2", "3", "4", "5"} );
        TestUtil.testArray( getStringValues("listTest2", "notation", "group", "A", "maxEx-notation", "2",  "_sort", "notation"), new String[] {"1"} );
        TestUtil.testArray( getStringValues("listTest2", "notation", "group", "A", "max-notation", "2",  "_sort", "notation"), new String[] {"1", "2"} );

        TestUtil.testArray( getStringValues("listTest2", "notation", "minEx-narrower.notation", "2",  "_sort", "notation"), new String[] {"3", "4", "5"} );
    }
    
    @Test
    public void testNested() {
        assertEquals(10, getAndCount("listTest2nested"));
        assertEquals("A1", getFirstLabel("listTest2nested", "_sort", "label"));
        assertEquals("B5", getFirstLabel("listTest2nested", "_sort", "-label"));
        assertEquals("B1", getFirstLabel("listTest2nested", "_sort", "narrower.label"));
        assertEquals("A5", getFirstLabel("listTest2nested", "_sort", "-narrower.label"));
    }

    @Test
    public void testTemplate() {
        assertEquals(1, getAndCount("listTestTemplate", "num", "1"));
        assertEquals("B1", getFirstLabel("listTestTemplate", "num", "1"));
        assertEquals(0, getAndCount("listTestTemplate", "num", "9"));
    }

    @Test
    public void testBindings() {
        assertEquals("B2", getFirstLabel("listTestTemplateBinding"));
        assertEquals("B1", getFirstLabel("listTestTemplateBinding", "num", "1"));
    }
    
    @Test
    public void testExclusion() {
        assertEquals(2, numTypesFirst("list-with-no-exclusion", "notation", "1"));
        assertEquals(1, numTypesFirst("list-with-exclusion", "notation", "1"));
    }
    
    private int numTypesFirst(String endpoint, String...args) {
        ResultStream stream = get(endpoint, makeRequest(args));
        assertTrue( stream.hasNext() );
        JsonValue jv =  stream.next().asJson();
        assertTrue( jv.isObject() );
        jv = jv.getAsObject().get("type");
        if (jv == null) {
            return 0;
        } else if (jv.isArray()) {
            return jv.getAsArray().size();
        } else {
            return 1;
        }
    }

    
    @Test
    public void testTimestamp() throws InterruptedException {
        LastModified lm = api.getTimestampService();
        assertNotNull(lm);
        
        Long ts = lm.getTimestamp( (SparqlDataSource) source);
        assertNotNull(ts);
        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z", Locale.UK);
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals("22 Sep 2016 20:05:05 GMT", fmt.format( new Date(ts) ) );
        Long lastChecked = lm.lastFetched();
        assertNotNull(lastChecked);
        
        Long ts2 = lm.getTimestamp( (SparqlDataSource) source);
        assertEquals(lastChecked, lm.lastFetched());
        assertEquals(ts, ts2);
        
        Thread.sleep(600);
        ts2 = lm.getTimestamp( (SparqlDataSource) source);
        assertTrue( lm.lastFetched() >= lastChecked + 500);
        assertEquals(ts, ts2);

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
    
    private List<String> getStringValues(String endpointName, String prop, String... args) {
        ResultStream stream = get(endpointName, makeRequest(args));
        List<String> labels = new ArrayList<>();
        for (Result r : stream) {
            TreeResult t = (TreeResult)r;
            Literal l = (Literal) t.getValues(prop).iterator().next();
            labels.add( l.getLexicalForm() );
        }
        return labels;
    }
}
