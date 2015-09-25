/******************************************************************
 * File:        TestAppAreas.java
 * Created by:  Dave Reynolds
 * Created on:  4 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileUtils;
import org.junit.Test;

import com.epimorphics.appbase.webapi.testing.TomcatTestBase;
import com.epimorphics.simpleAPI.util.JsonComparator;

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
    public void testEndToEnd() throws IOException {
        // Check explicit describe
        String testArea = "012WACTL12";
        Response response = getResponse(BASE_URL + "id/floodAreas/" + testArea, "application/json");
        checkJson(response, "src/test/data/TestApp/response-" + testArea + ".json");
        
        // Default describe
        response = getResponse(BASE_URL + "id/floods/90058", "application/json");
        checkJson(response, "src/test/data/TestApp/response-alert.json");
        
        // Describe by URI
        response = getResponse(BASE_URL + "byURITest?uri=http://environment.data.gov.uk/flood-monitoring/id/floods/90058", "application/json");
        checkJson(response, "src/test/data/TestApp/response-alert.json");

        // Describe, RDF
        response = getResponse(BASE_URL + "id/floods/90058", "text/turtle");
        checkRdf(response,  "src/test/data/TestApp/response-012WACTL12.ttl" );

        // Describe, Html
        response = getResponse(BASE_URL + "id/floods/90058", "text/html");
        checkText(response,  "src/test/data/TestApp/response-012WACTL12.txt" );

        // Explicit query from spec, with a map to JSON
        response = getResponse(BASE_URL + "fixedQueryTest", "application/json");
        checkJson(response, "src/test/data/TestApp/response-fixedQueryTest.json");
        
        // Binding a variable in a query (explicit bindVars)
        response = getResponse(BASE_URL + "paramFixedQueryTest?min-severity=1", "application/json");
        checkJson(response, "src/test/data/TestApp/response-paramQueryTest-1.json");
        
        // Explicit query, no map to JSON rely on defaults 
        response = getResponse(BASE_URL + "fixedQueryTestNoMap", "application/json");
        checkJson(response, "src/test/data/TestApp/response-fixedQueryTestNoMap.json");

        // Implicit query generated from JSON map
        response = getResponse(BASE_URL + "implicitQueryTest", "application/json");
        checkJson(response, "src/test/data/TestApp/response-fixedQueryTest.json");

        // Injecting filter into implicit query in the endpoint code
        response = getResponse(BASE_URL + "fixedQueryModTest?min-severity=1", "application/json");
        checkJson(response, "src/test/data/TestApp/response-paramQueryTest-1.json");

        // Injecting filter from filterable parameter
        response = getResponse(BASE_URL + "fixedQueryModTest?severityLevel=1", "application/json");
        checkJson(response, "src/test/data/TestApp/response-paramQueryTest-1.json");

        response = getResponse(BASE_URL + "fixedQueryModTest?severityLevel=illegal", "application/json");
        assertEquals(400, response.getStatus());
        
        // Injecting basic limit/offsets
        response = getResponse(BASE_URL + "fixedQueryModTest?min-severity=2&_offset=1&_limit=2", "application/json");
        checkJson(response, "src/test/data/TestApp/response-min2offset1limit2.json");
        
        // Soft/hard limits
        response = getResponse(BASE_URL + "alertTestLimit", "application/json");
        checkResultSize(response, 2); 
        response = getResponse(BASE_URL + "alertTestLimit?_limit=3", "application/json");
        checkResultSize(response, 3); 
        response = getResponse(BASE_URL + "alertTestLimit?_limit=999", "application/json");
        checkResultSize(response, 4); 
        
        // Implicit query, CSV serialization
        response = getResponse(BASE_URL + "implicitQueryTest", "text/csv");
        checkText(response, "src/test/data/TestApp/response-implicitQuery.csv");
        
//        assertEquals(200, response.getStatus());
//        System.out.println( response.getEntity(String.class) );
    }

    private void checkJson(Response response, String expected) throws IOException {
        assertEquals(200, response.getStatus());
        
        JsonObject json = JSON.parse( response.readEntity(InputStream.class) );
        JsonObject expectedJson = JSON.parse( FileUtils.readWholeFileAsUTF8(expected) );
        assertTrue( JsonComparator.equal(expectedJson, json) );
    }
    
    private void checkRdf(Response response, String expected) throws IOException {
        assertEquals(200, response.getStatus());
        
        Model actual = ModelFactory.createDefaultModel();
        actual.read(response.readEntity(InputStream.class), null, "Turtle");
        if (expected == null) {
            actual.write(System.out, "Turtle");
        } else {
            actual.write(System.out, "Turtle");
            Model mexpected = RDFDataMgr.loadModel(expected);
            assertTrue( mexpected.isIsomorphicWith(actual) );
        }
    }

    private void checkText(Response response, String expected) throws IOException {
        assertEquals(200, response.getStatus());
        String entity = response.readEntity(String.class);
        if (expected == null) {
            System.out.println(entity);
        } else {
            String expectedString = FileManager.get().readWholeFileAsUTF8(expected);
            entity = entity.replace("\r", "");   // Normalize csv
            assertEquals(expectedString, entity);
        }
    }
    
    private void checkResultSize(Response response, int expected) {
        assertEquals(200, response.getStatus());
        
        JsonObject json = JSON.parse( response.readEntity(InputStream.class));
        JsonArray items = json.get("items").getAsArray();
        assertEquals(expected, items.size());
    }
}
