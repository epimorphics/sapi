/******************************************************************
 * File:        TestAppAreas.java
 * Created by:  Dave Reynolds
 * Created on:  4 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.appbase.webapi.testing.TomcatTestBase;
import com.epimorphics.simpleAPI.util.JsonComparator;
import com.hp.hpl.jena.util.FileUtils;
import com.sun.jersey.api.client.ClientResponse;

/**
 * End-to-end API tests which fire up TestApp, loaded with flood area data, and
 * checks API endpoint access.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TestAppAreas extends TomcatTestBase {

    @Override
    public String getWebappRoot() {
        return "src/test/TestApp";
    }

    @Test
    public void testDummy() throws IOException {
        String testArea = "012WACTL12";
        ClientResponse response = getResponse(BASE_URL + "id/floodAreas/" + testArea, "application/json");
        checkJson(response, "src/test/data/TestApp/response-" + testArea + ".json");
        
        response = getResponse(BASE_URL + "id/floods/90058", "application/json");
        checkJson(response, "src/test/data/TestApp/response-alert.json");
        
        response = getResponse(BASE_URL + "fixedQueryTest", "application/json");
        checkJson(response, "src/test/data/TestApp/response-fixedQueryTest.json");
        
        response = getResponse(BASE_URL + "paramFixedQueryTest?min-severity=1", "application/json");
        checkJson(response, "src/test/data/TestApp/response-paramQueryTest-1.json");
        
//        assertEquals(200, response.getStatus());
//        System.out.println( response.getEntity(String.class) );
    }
    
    private void checkJson(ClientResponse response, String expected) throws IOException {
        assertEquals(200, response.getStatus());
        
        JsonObject json = JSON.parse( response.getEntityInputStream() );
        JsonObject expectedJson = JSON.parse( FileUtils.readWholeFileAsUTF8(expected) );
        assertTrue( JsonComparator.equal(expectedJson, json) );
    }
}
