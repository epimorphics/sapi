/******************************************************************
 * File:        TestRequestChecker.java
 * Created by:  Dave Reynolds
 * Created on:  19 Nov 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.webapi.test.MockUriInfo;

/**
 * Tests for request/parameter checking utilities
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TestRequestChecker {
    protected RequestCheck checks = new RequestCheck();
    protected API api = new API();
    
    @Before
    public void setUp() {
        api.setBaseURI("http://localhost/");
        checks.addParameterCheck( new ParameterCheck("foo", true) );
        checks.addParameterCheck( new ParameterCheck("bar", "\\d\\d") );
        checks.addParameterCheck( new ParameterCheck("age", ".*", "42") );
    }
    
    @Test
    public void testBasics() {
        Request request = checkRequest("http://localhost?foo=a&bar=28", true);
        assertEquals("42", request.getFirst("age"));
        checkRequest("http://localhost?foo=a&bar=280", false);
        checkRequest("http://localhost?bar=28", false);
        request = checkRequest("http://localhost?foo=a&bar=28&age=58", true);
        assertEquals("58", request.getFirst("age"));
    }
    
    @Test
    public void testJsonParse() {
        JsonObject json = JSON.read("src/test/data/requestChecks/check.json");
        RequestCheck rc = RequestCheck.fromJson(json);
        assertEquals(4, rc.getChecks().size());
        
        ParameterCheck c = rc.getChecks().get(0);
        assertEquals("foo", c.getParameter());
        assertEquals(true, c.isRequired());
        
        c = rc.getChecks().get(1);
        assertEquals("bar", c.getParameter());
        assertEquals("\\d\\d", c.getRegex());
        
        c = rc.getChecks().get(2);
        assertEquals("age", c.getParameter());
        assertEquals("42", c.getDeflt());
        
        c = rc.getChecks().get(3);
        assertEquals("baz", c.getParameter());
        assertEquals(".*", c.getRegex());
        assertEquals(true, c.isRequired());
    }
    
    protected Request checkRequest(String request, boolean expectOK) {
        Request r = Request.from(api, new MockUriInfo(request), null);
        String error = null;
        try {
            checks.checkRequest(r);
        } catch (WebApiException e) {
            error = e.getMessage();
        }
        if (expectOK) {
            assertNull(error, error);
        } else {
            assertNotNull(error);
        }
        return r;
    }
}
