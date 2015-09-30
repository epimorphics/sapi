/******************************************************************
 * File:        TestResultBasics.java
 * Created by:  Dave Reynolds
 * Created on:  30 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.jena.rdf.model.Literal;
import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.impl.SparqlListEndpointSpec;
import com.epimorphics.simpleAPI.query.DataSource;
import com.epimorphics.simpleAPI.query.Query;

public class TestResultBasics {
    App app;
    API api;
    DataSource source;
    
    @Before
    public void setUP() throws IOException {
        app = new App("test", new File("src/test/baseResultTest/app.conf"));
        api = app.getA(API.class);
        app.startup();
        source = app.getA(DataSource.class);
    }

    @Test
    public void testSimpleResultStream() {
        SparqlListEndpointSpec spec = (SparqlListEndpointSpec) api.getSpec("listTest1");
        assertNotNull(spec);
        assertNotNull(spec.getQueryBuilder());
        assertNotNull(spec.getView());
        
        Query query = spec.getQueryBuilder().sort("notation", false).build();
        ResultStream stream = source.query(query, spec.getView());
        assertTrue( stream.hasNext() );
        checkEntryRoot( stream.next(), 1);
        assertTrue( stream.hasNext() );
        checkEntryRoot( stream.next(), 2);
        assertFalse( stream.hasNext() );
    }
    
    private void checkEntryRoot(Result result, int index) {
        assertEquals( "http://localhost/example/A" + index, result.getId().asResource().getURI() );
        assertEquals( "" + index + 1, asLex( result.getValues("notation").iterator().next() ) );
        // TODO test labels
    }
    
    private String asLex(Object value) {
        assertTrue(value instanceof Literal);
        return ((Literal) value).getLexicalForm();
    }
}
