/******************************************************************
 * File:        TestResultBasics.java
 * Created by:  Dave Reynolds
 * Created on:  30 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Literal;
import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.endpoints.impl.SparqlListEndpointSpec;
import com.epimorphics.simpleAPI.query.DataSource;
import com.epimorphics.simpleAPI.query.ItemQuery;
import com.epimorphics.simpleAPI.query.ListQuery;
import com.epimorphics.simpleAPI.query.ListQueryBuilder;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.requests.Request;
import com.epimorphics.simpleAPI.util.JsonComparator;

public class TestResultBasics {
    App app;
    API api;
    DataSource source;
    
    @Before
    public void setUP() throws IOException {
        app = new App("test", new File("src/test/testCases/baseResultTest/app.conf"));
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
        
        // Basic case, no nesting
        ListQueryBuilder builder = (ListQueryBuilder) spec.getQueryBuilder();
        ListQuery query = builder.sort("notation", false).build();
        ResultStream stream = source.query(query, new Call(spec, null));
        for (int i = 1; i <= 2; i++){
            assertTrue( stream.hasNext() );
            checkEntryRoot( (TreeResult)stream.next(), i);
        }
        assertFalse( stream.hasNext() );
        
        // Case with nesting
        spec = (SparqlListEndpointSpec) api.getSpec("listTest2");
        assertNotNull(spec);
        builder = (ListQueryBuilder) spec.getQueryBuilder();
        query = builder.sort("notation", false).build();
        stream = source.query(query, new Call(spec, null));
        for (int i = 1; i <= 2; i++){
            assertTrue( stream.hasNext() );
            TreeResult r = (TreeResult) stream.next();
            checkEntryRoot( r, i);
            String children = r.getSortedValues("narrower").stream().map(v -> v instanceof TreeResult ? ((TreeResult)v).getId().toString() : "Not nested").collect(Collectors.joining(","));
            assertEquals( "http://localhost/example/B%,http://localhost/example/C%".replace("%", Integer.toString(i)), children );
        }
        assertFalse( stream.hasNext() );
        
        // Nested case, check JSON render
        stream = source.query(query, new Call(spec, null));
        assertTrue( JsonComparator.equal("src/test/testCases/baseResultTest/expected/r1.json", stream.next().asJson()) );
        assertTrue( JsonComparator.equal("src/test/testCases/baseResultTest/expected/r2.json", stream.next().asJson()) );
        assertFalse( stream.hasNext() );
    }
    
    @Test
    public void testDescribe() {
        EndpointSpec spec = api.getSpec("itemTest1");
        assertNotNull(spec);
        
        String URI = "http://localhost/example/A2";
        Request request = new Request(URI);
        ItemQuery query = (ItemQuery) spec.getQueryBuilder( request ).build();
        assertTrue( query.toString().contains(URI) );
        
        Result result = source.query(query, new Call(spec, request) );
        assertTrue( JsonComparator.equal("src/test/testCases/baseResultTest/expected/itemTest1.json", result.asJson()) );
        
        spec = api.getSpec("itemTest2");
        assertNotNull(spec);
        query = (ItemQuery) spec.getQueryBuilder( request ).build();
        result = source.query(query, new Call(spec, request) );
        assertTrue( JsonComparator.equal("src/test/testCases/baseResultTest/expected/itemTest2.json", result.asJson()) );
        
        spec = api.getSpec("itemTest3");
        assertNotNull(spec);
        query = (ItemQuery) spec.getQueryBuilder( request ).build();
        result = source.query(query, new Call(spec, request) );
        assertTrue( JsonComparator.equal("src/test/testCases/baseResultTest/expected/itemTest2.json", result.asJson()) );
    }
    
    private void checkEntryRoot(TreeResult result, int index) {
        assertEquals( "http://localhost/example/A" + index, result.getId().asResource().getURI() );
        assertEquals( "" + index + 1, asLex( result.getValues("notation").iterator().next() ) );
        String labels = result.getSortedValues("label").stream().map(TestResultBasics::asLex).collect(Collectors.joining(","));
        assertEquals("A%,a%".replace("%", Integer.toString(index)), labels);
    }
    
    private static String asLex(Object value) {
        assertTrue(value instanceof Literal);
        return ((Literal) value).getLexicalForm();
    }
}
