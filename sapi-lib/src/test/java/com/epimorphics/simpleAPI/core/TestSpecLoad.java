/******************************************************************
 * File:        TestEndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import com.epimorphics.appbase.core.App;
import com.epimorphics.simpleAPI.core.API;

public class TestSpecLoad {
    App app;
    API api;
    
    @Before
    public void setUP() throws IOException {
        app = new App("test", new File("src/test/baseEPTest/app.conf"));
        api = app.getA(API.class);
        app.startup();
    }

    @Ignore
    @Test
    public void testEndpointsExist() {
        assertNotNull( api.getSpec("describeTest") );
        assertNotNull( api.getSpec("listTest") );
        assertNull( api.getSpec("otherTest") );
    }
    
    @Test
    public void testViewLoading() {
        assertNull( api.getSpec("otherTest") );
        ViewMap view = api.getView("view-test"); 
        assertNotNull( view );
        
        assertEquals( "view-test", view.getName() );
        List<ViewEntry> children = view.getTree().getChildren();
        assertEquals(4, children.size());
        checkEntry(children.get(0), "severity",  "rt:severity",  false, false, false,  true);
        checkEntry(children.get(1), "message",   "rt:message",   false,  true, false,  true);
        checkEntry(children.get(2), "floodArea", "rt:floodArea",  true, false, false,  true);
        checkEntry(children.get(3), "test",      "rt:test",      false, false,  true, false);
        
        // nested block
        children = children.get(2).getNested().getChildren();
        checkEntry(children.get(0), "notation",  "skos:notation",  false, false, false, true);
        checkEntry(children.get(1), "county",    "rt:county",      false, false, false, true);
    }
    
    private void checkEntry(ViewEntry entry, String json, String prop, boolean nested, boolean optional, boolean multi, boolean filterable) {
        assertEquals(json, entry.getJsonName());
        assertEquals(prop, entry.getProperty());
        assertEquals(nested, entry.isNested());
        assertEquals(optional, entry.isOptional());
        assertEquals(multi, entry.isMultivalued());
        assertEquals(filterable, entry.isFilterable());
    }
}
