/******************************************************************
 * File:        TestEndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.simpleAPI.endpoints.impl.SparqlListEndpointSpec;
import com.epimorphics.simpleAPI.query.Query;
import com.epimorphics.simpleAPI.query.impl.SparqlQuery;
import com.epimorphics.simpleAPI.views.ViewEntry;
import com.epimorphics.simpleAPI.views.ViewMap;

public class TestSpecLoad {
    App app;
    API api;
    
    @Before
    public void setUP() throws IOException {
        app = new App("test", new File("src/test/baseEPTest/app.conf"));
        api = app.getA(API.class);
        app.startup();
    }

    @Test
    public void testEndpointsExist() {
        assertNotNull( api.getSpec("listTest") );
        SparqlListEndpointSpec spec = (SparqlListEndpointSpec) api.getSpec("listTest");
        assertEquals(10, spec.getSoftLimit().longValue());
        assertEquals(100, spec.getHardLimit().longValue());
        Query query = spec.getQueryBuilder().build();
        String qStr = ((SparqlQuery)query).getQuery();        
        assertTrue(  qStr.contains("?id a rt:FloodAlertOrWarning") );
        assertTrue( qStr.contains("PREFIX rt: <http://environment.data.gov.uk/flood-monitoring/def/core/>") );
        
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
    
    @Test
    public void testViewVarname() {
        ViewMap view = api.getView("varnameTest");
        assertNotNull(view);
        
        assertEquals("foo", view.asVariableName("foo"));
        assertEquals("foo_bar", view.asVariableName("bar"));
        assertEquals("foo_bar_test", view.asVariableName("test"));
        assertEquals("foo_baz_label", view.asVariableName("foo.baz.label"));
        assertEquals("label", view.asVariableName("label"));
        assertEquals("foo_fu__bar", view.asVariableName("fu_bar"));
        assertEquals("foo_fu__bar", view.asVariableName("foo.fu_bar"));
        
        assertEquals("foo.fu_bar", view.getTree().pathTo("fu_bar").asDotted());
    }
}
