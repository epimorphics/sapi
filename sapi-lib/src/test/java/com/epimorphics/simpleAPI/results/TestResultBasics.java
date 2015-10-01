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
import java.util.stream.Collectors;

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
        for (int i = 1; i <= 2; i++){
            assertTrue( stream.hasNext() );
            checkEntryRoot( stream.next(), i);
        }
        assertFalse( stream.hasNext() );
        
        spec = (SparqlListEndpointSpec) api.getSpec("listTest2");
        assertNotNull(spec);
        query = spec.getQueryBuilder().sort("notation", false).build();
        stream = source.query(query, spec.getView());
        for (int i = 1; i <= 2; i++){
            assertTrue( stream.hasNext() );
            Result r = stream.next();
            checkEntryRoot( r, i);
            String children = r.getSortedValues("narrower").stream().map(v -> v instanceof Result ? ((Result)v).getId().toString() : "Not nested").collect(Collectors.joining(","));
            assertEquals( "http://localhost/example/B%,http://localhost/example/C%".replace("%", Integer.toString(i)), children );
        }
        assertFalse( stream.hasNext() );
    }
    
    private void checkEntryRoot(Result result, int index) {
        assertEquals( "http://localhost/example/A" + index, result.getId().asResource().getURI() );
        assertEquals( "" + index + 1, asLex( result.getValues("notation").iterator().next() ) );
        String labels = result.getValues("label").stream().map(TestResultBasics::asLex).sorted().collect(Collectors.joining(","));
        assertEquals("A" + index + ",a" + index, labels);
    }
    
    private static String asLex(Object value) {
        assertTrue(value instanceof Literal);
        return ((Literal) value).getLexicalForm();
    }
}
