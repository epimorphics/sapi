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
import org.junit.Test;

import com.epimorphics.appbase.webapi.testing.TomcatTestBase;
import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.util.JsonComparator;

public class TestBaseEndToEnd extends TomcatTestBase {
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
        checkGet("basetest/list?_limit=2&_sort=label", "src/test/testCases/baseEndToEndTest/expected/list-limit2.json");
        checkGet("basetest/list?_limit=2&_sort=@id", "src/test/testCases/baseEndToEndTest/expected/list-limit2-id.json");
        checkGet("basetest/list?group=B&_limit=2&_sort=label", "src/test/testCases/baseEndToEndTest/expected/list-filterB-limit2.json");
        
        checkGet("basetest/list?_view=compact&_limit=2&_sort=label", "src/test/testCases/baseEndToEndTest/expected/list-compact-limit2.json");
        
        checkGet("default/test3?_sort=label", "src/test/testCases/baseEndToEndTest/expected/list-default-test3.json");
        checkGet("default/test4/B?_sort=label", "src/test/testCases/baseEndToEndTest/expected/list-default-test4-B.json");
        
        checkPost("basetest/list", JsonUtil.makeJson("group", "B", "_limit", 2, "_sort", "label"), "src/test/testCases/baseEndToEndTest/expected/list-filterB-limit2-post.json");
        checkPost("default/test4/B", JsonUtil.makeJson("_sort", "label"), "src/test/testCases/baseEndToEndTest/expected/list-default-test4-B-post.json");
        
        // Describe checks
        checkGet("example/A2",  "src/test/testCases/baseEndToEndTest/expected/describe-A2.json");
        checkGetTtl("example/A2", "src/test/testCases/baseEndToEndTest/expected/describe-A2.ttl");
        assertEquals(404, getResponse(BASE_URL + "example/notThere", "application/json").getStatus());
        assertEquals(404, getResponse(BASE_URL + "example/notThere", "text/turtle").getStatus());

        // RDF serialization
        checkGetTtl("basetest/list?_limit=2&_sort=@id", "src/test/testCases/baseEndToEndTest/expected/list-limit2-id.ttl");
        
    }
    
    protected void checkGet(String url, String expectedF) {
        checkResponse( getResponse( BASE_URL + url, "application/json"), expectedF );
    }
    
    protected void checkGetTtl(String url, String expectedF) {
        checkResponseTtl( getResponse( BASE_URL + url, "text/turtle"), expectedF );
    }
    
    protected void checkPost(String url, JsonObject body, String expectedF) {
        WebTarget r = c.target( BASE_URL + url );
        Response response = r.request(MediaType.APPLICATION_JSON).post(Entity.entity(body.toString(), MediaType.APPLICATION_JSON));
        checkResponse(response, expectedF);
    }
    
    protected void checkResponse(Response response, String expectedF) {
        if (response.getStatus() != 200) {
            System.err.println( String.format("[%d] %s", response.getStatus(), response.readEntity(String.class)));
            assertEquals(200, response.getStatus());
        }
        String entity = response.readEntity(String.class);
        JsonObject actual = JSON.parseAny( entity ).getAsObject();
        if (expectedF == null) {
            System.out.println("Test incomplete, actual was: " + entity);
        } else {
            JsonObject expected = JSON.read(expectedF);
            assertTrue( JsonComparator.equal(expected, actual) );
        }
    }
    
    protected void checkResponseTtl(Response response, String expectedF) {
        if (response.getStatus() != 200) {
            System.err.println( String.format("[%d] %s", response.getStatus(), response.readEntity(String.class)));
            assertEquals(200, response.getStatus());
        }
        InputStream in = response.readEntity(InputStream.class);
        Model actual = ModelFactory.createDefaultModel();
        RDFDataMgr.read(actual, in, Lang.TTL);
        if (expectedF == null) {
            System.out.println("Test incomplete, actual was: ");
            actual.write(System.out, "Turtle");
        } else {
            Model expected = RDFDataMgr.loadModel(expectedF);
            assertTrue( actual.isIsomorphicWith(expected) );
        }
    }
}
