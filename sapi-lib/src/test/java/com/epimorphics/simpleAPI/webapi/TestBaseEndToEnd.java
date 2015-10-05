/******************************************************************
 * File:        TestBaseEndToEnd.java
 * Created by:  Dave Reynolds
 * Created on:  5 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;
import static org.junit.Assert.*;

import com.epimorphics.appbase.webapi.testing.TomcatTestBase;
import com.epimorphics.simpleAPI.util.JsonComparator;

public class TestBaseEndToEnd extends TomcatTestBase {
    @Override
    public String getWebappContext() {
        return "";
    }

    @Override
    public String getWebappRoot() {
        return "src/test/baseEndToEndTest";
    }

    @Test
    public void testEndToEnd() throws IOException {
        checkJSON("basetest/list?_limit=2&_sort=label", "src/test/baseEndToEndTest/expected/list-limit2.json");
        checkJSON("basetest/list?group=B&_limit=2&_sort=label", "src/test/baseEndToEndTest/expected/list-filterB-limit2.json");
        
        checkJSON("basetest/list?_view=compact&_limit=2&_sort=label", "src/test/baseEndToEndTest/expected/list-compact-limit2.json");
    }
    
    protected void checkJSON(String url, String expectedF) {
        Response response = getResponse( BASE_URL + url, "application/json");
        String entity = response.readEntity(String.class);
        JsonObject actual = JSON.parseAny( entity ).getAsObject();
        if (expectedF == null) {
            System.out.println("Test incompleted, actual was: " + entity);
        } else {
            JsonObject expected = JSON.read(expectedF);
            assertTrue( JsonComparator.equal(expected, actual) );
        }
    }
}
