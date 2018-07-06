/******************************************************************
 * File:        TestBaseEndToEnd.java
 * Created by:  Dave Reynolds
 * Created on:  5 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.junit.Test;

import com.epimorphics.appbase.webapi.testing.TomcatTestBase;
import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.util.JsonComparator;

public class TestBaseEndToEnd extends TomcatTestBase {
    static final String EXPECTED = "src/test/testCases/baseEndToEndTest/expected/";
    
    @Override
    public String getWebappContext() {
        return "";
    }

    @Override
    public String getWebappRoot() {
        return "src/test/testCases/baseEndToEndTest";
    }

    @Test
    public void testEndToEnd() throws IOException {
        checkGet("basetest/list?_limit=2&_sort=label", EXPECTED + "list-limit2.json");
        checkGet("basetest/list?_limit=2&_sort=@id", EXPECTED + "list-limit2-id.json");
        checkGet("basetest/list?group=B&_limit=2&_sort=label", EXPECTED + "list-filterB-limit2.json");
        
        checkGet("basetest/list?_view=compact&_limit=2&_sort=label", EXPECTED + "list-compact-limit2.json");
        checkGet("basetest/list?_view=expanded&_limit=2&_sort=label", EXPECTED + "list-expanded-limit2.json");
        
        checkGet("default/test3?_sort=label", EXPECTED + "list-default-test3.json");
        checkGet("default/test4/B?_sort=label", EXPECTED + "list-default-test4-B.json");
        
        checkPost("basetest/list", JsonUtil.makeJson("group", "B", "_limit", 2, "_sort", "label"), EXPECTED + "list-filterB-limit2-post.json");
        checkPost("default/test4/B", JsonUtil.makeJson("_sort", "label"), EXPECTED + "list-default-test4-B-post.json");
        
        // Filter with localname expansion
        checkGet("basetest/list?narrower=A1", EXPECTED + "list-filter-A1local.json");
        
        // Check version with extra nest level
        checkGet("basetest/listNested?narrower=A1", EXPECTED + "list-nested-A1.json");
        
        // Describe checks
        checkGet("example/A2",  EXPECTED + "describe-A2.json");
        assertEquals(404, getResponse(BASE_URL + "example/notThere", "application/json").getStatus());
        assertEquals(404, getResponse(BASE_URL + "example/notThere", "text/turtle").getStatus());

        // RDF serialization
        checkGetTtl("basetest/list?_limit=2&_sort=@id", EXPECTED + "list-limit2-id.ttl");
        checkGetTtl("example/A2", EXPECTED + "describe-A2.ttl");

        // CSV serialization
        checkGetCSV("basetest/list?_limit=2&_sort=@id", EXPECTED + "list-limit2-id.csv");
        checkGetCSV("basetest/listSuppress?_limit=2&_sort=@id", EXPECTED + "list-limit2-suppress.csv");
        
        Response response = getResponse(BASE_URL + "basetest/listSuppress?_limit=2&_sort=@id&_format=csv");
        checkResponseCSV(response, EXPECTED + "list-limit2-suppress.csv");
        
        // HTML serialization
        checkResponseText(
                getResponse(BASE_URL + "basetest/list?_limit=2&_sort=@id", "text/html"),
                EXPECTED + "list-limit2-id2.html" );
       
        // Use of nested selects
        checkGet("basetest/listNestedSelect?_sort=@id", EXPECTED + "listNestedSelect-base.json");
        checkGet("basetest/listNestedSelect?filter=test&_sort=@id", EXPECTED + "listNestedSelect-filter.json");
        checkGet("basetest/listNestedSelect?_limit=2&_sort=@id", EXPECTED + "listNestedSelect-limit2.json");
        checkGet("basetest/listNestedSelect?narrower.notation=3&_sort=@id", EXPECTED + "listNestedSelect-filterGeneric.json");
        
        // Nested view patters with underscores in the property name
        checkGet("basetest/listUS?_sort=@id", EXPECTED + "list-underscores.ttl");
        
    }
    
    protected void checkGet(String url, String expectedF) {
        checkResponse( getResponse( BASE_URL + url, "application/json"), expectedF );
    }
    
    protected void checkGetTtl(String url, String expectedF) {
        checkResponseTtl( getResponse( BASE_URL + url, "text/turtle"), expectedF );
    }
    
    protected void checkGetCSV(String url, String expectedF) {
        checkResponseCSV( getResponse( BASE_URL + url, "text/csv"), expectedF );
    }
    
    protected void checkPost(String url, JsonObject body, String expectedF) {
        WebTarget r = c.target( BASE_URL + url );
        Response response = r.request(MediaType.APPLICATION_JSON).post(Entity.entity(body.toString(), MediaType.APPLICATION_JSON));
        checkResponse(response, expectedF);
    }
    
    protected void checkResponse(Response response, String expectedF) {
        checkStatus(response);
        String entity = response.readEntity(String.class);
        JsonObject actual = JSON.parseAny( entity ).getAsObject();
        if (expectedF == null) {
            System.out.println("Test incomplete, actual was: " + entity);
        } else {
            JsonObject expected = JSON.read(expectedF);
            boolean equal = JsonComparator.equal(expected, actual);
            if (!equal) {
            	System.err.println(">> expected: " + expected);
            	System.err.println(">> actual:   " + actual);
            }
			assertTrue( equal );
        }
    }

    private void checkStatus(Response response) {
        if (response.getStatus() != 200) {
            System.err.println( String.format("[%d] %s", response.getStatus(), response.readEntity(String.class)));
            assertEquals(200, response.getStatus());
        }
    }
    
    protected void checkResponseTtl(Response response, String expectedF) {
        checkStatus(response);
        InputStream in = response.readEntity(InputStream.class);
        Model actual = ModelFactory.createDefaultModel();
        RDFDataMgr.read(actual, in, Lang.TTL);
        if (expectedF == null) {
            System.out.println("Test incomplete, actual was:\n");
            actual.write(System.out, "Turtle");
        } else {
            Model expected = RDFDataMgr.loadModel(expectedF);
            assertTrue( actual.isIsomorphicWith(expected) );
        }
    }
    
    protected void checkResponseCSV(Response response, String expectedF) {
        checkStatus(response);
        String entity = response.readEntity(String.class);
        if (expectedF == null) {
            System.out.println("Test incomplete, actual was" + entity);
        } else {
            String expected = FileManager.get().readWholeFileAsUTF8(expectedF).replace("\n", "\r\n");
            assertEquals(expected, entity);
        }
    }
    
    protected void checkResponseText(Response response, String expectedF) {
        checkStatus(response);
        String entity = response.readEntity(String.class);
        if (expectedF == null) {
            System.out.println("Test incomplete, actual was" + entity);
        } else {
            String expected = FileManager.get().readWholeFileAsUTF8(expectedF);
            assertEquals(expected, entity);
        }
    }
}
