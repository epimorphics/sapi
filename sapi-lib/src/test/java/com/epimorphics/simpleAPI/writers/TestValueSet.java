/******************************************************************
 * File:        TestStreamCoalesce.java
 * Created by:  Dave Reynolds
 * Created on:  10 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createPlainLiteral;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.jena.riot.RDFDataMgr;
import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.EndpointSpec;
import com.epimorphics.simpleAPI.core.EndpointSpecFactory;
import com.epimorphics.simpleAPI.core.JSONMap;
import com.epimorphics.simpleAPI.core.RequestParameters;
import com.epimorphics.util.PrefixUtils;
import com.epimorphics.util.TestUtil;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestValueSet {
    App app;
    API api;
    
    @Before
    public void setUP() throws IOException {
        app = new App("test", new File("src/test/TestApp/WEB-INF/static-app.conf"));
        api = app.getA(API.class);
    }
    
    /**
     * Test basic case with no explicit map for conversion
     */
    @Test
    public void testBasicStreamWrapper() {
        Model data = RDFDataMgr.loadModel("src/test/data/writer/streamtest.ttl");
        String query = "SELECT * WHERE {?id rdfs:label ?label; rdf:value ?value. OPTIONAL {?id rdfs:comment ?comment} } ORDER BY ?id";
        query = PrefixUtils.expandQuery(query, PrefixUtils.commonPrefixes());
        QueryExecution qexec = QueryExecutionFactory.create(query, data);
        try {
            ValueStream stream = new ValueStream(qexec.execSelect());
            assertTrue(stream.hasNext());
            
            ValueSet result = stream.next();
            checkSingleString(result, "label", "resource 1");
            checkSingleString(result, "comment", "optional");
            checkStrings(result, "value", "1");
            
            result = stream.next();
            checkSingleString(result, "label", "resource 2");
            assertNull( result.getKeyValues("comment") );
            checkStrings(result, "value", "2", "12");
            
            result = stream.next();
            checkSingleString(result, "label", "resource 3");
            checkStrings(result, "value", "3", "13", "23");
            
            assertFalse(stream.hasNext());
        } finally {
            qexec.close();
        }
    }
    
    /**
     * Simple nested map
     */
    @Test
    public void testNested() {
        Model data = RDFDataMgr.loadModel("src/test/data/writer/streamtest2.ttl");
        EndpointSpec ep = EndpointSpecFactory.read(api, "src/test/data/writer/streamtest2.yaml");
        String query = ep.getQuery(new RequestParameters("http://dummy"));
        QueryExecution qexec = QueryExecutionFactory.create(query, data);
        try {
            ValueStream stream = new ValueStream(qexec.execSelect(), ep.getMap());
            assertTrue(stream.hasNext());
            
            ValueSet result = stream.next();
            checkSingleString(result, "label", "resource 1");
            assertNull( result.getKeyValues("nlabel") );
            assertNull( result.getKeyValues("nvalue") );
            KeyValues nest = result.getKeyValues("value");
            assertNotNull(nest);
            assertTrue( nest.getValue() instanceof ValueSet );
            ValueSet nestvs = (ValueSet) nest.getValue();
            assertEquals( "http://localhost/nest1",  nestvs.getStringID() );
            checkSingleString(nestvs, "nlabel", "nest 1");
            checkSingleString(nestvs, "nvalue", "1");
            
            result = stream.next();
            checkSingleString(result, "label", "resource 2");
            assertNull( result.getKeyValues("nlabel") );
            assertNull( result.getKeyValues("nvalue") );
            nest = result.getKeyValues("value");
            assertNotNull(nest);
            List<Object> values = nest.getValues();
            assertEquals(2, values.size());
            assertTrue(
                (   valueIs(values.get(0), "http://localhost/nest2", "nest 2", "2") 
                 && valueIs(values.get(1), "http://localhost/nest3", "nest 3", "3") )
             || (   valueIs(values.get(1), "http://localhost/nest2", "nest 2", "2") 
                 && valueIs(values.get(0), "http://localhost/nest3", "nest 3", "3") )
            );
            
            assertFalse( stream.hasNext());
        } finally {
            qexec.close();
        }
    }
    
    /**
     * Two deep nested map
     */
    @Test
    public void testNested3() {
        Model data = RDFDataMgr.loadModel("src/test/data/writer/streamtest3.ttl");
        EndpointSpec ep = EndpointSpecFactory.read(api, "src/test/data/writer/streamtest3.yaml");
        String query = ep.getQuery(new RequestParameters("http://dummy"));
        QueryExecution qexec = QueryExecutionFactory.create(query, data);
        try {
            ValueStream stream = new ValueStream(qexec.execSelect(), ep.getMap());
            assertTrue(stream.hasNext());
            
            ValueSet result = stream.next();
            checkSingleString(result, "label", "resource 1");
            assertNull( result.getKeyValues("nlabel") );
            assertNull( result.getKeyValues("nnlabel") );
            assertNull( result.getKeyValues("nvalue") );
            KeyValues nest = result.getKeyValues("value");
            assertNotNull(nest);
            assertTrue( nest.getValue() instanceof ValueSet );
            ValueSet nestvs = (ValueSet) nest.getValue();
            assertEquals( "http://localhost/nest1",  nestvs.getStringID() );
            checkSingleString(nestvs, "nlabel", "nest 1");
            valueIs3(nestvs.getKeyValues("nvalue").getValue(), "http://localhost/nest1a", "nest 1a");
            
            result = stream.next();
            checkSingleString(result, "label", "resource 2");
            assertNull( result.getKeyValues("nlabel") );
            assertNull( result.getKeyValues("nnlabel") );
            assertNull( result.getKeyValues("nvalue") );
            nest = result.getKeyValues("value");
            assertNotNull(nest);
            assertTrue( nest.getValue() instanceof ValueSet );
            nestvs = (ValueSet) nest.getValue();
            assertEquals( "http://localhost/nest2",  nestvs.getStringID() );
            checkSingleString(nestvs, "nlabel", "nest 2");
            valueIs3(nestvs.getKeyValues("nvalue").getValue(), "http://localhost/nest2a", "nest 2a");

            assertFalse( stream.hasNext());
        } finally {
            qexec.close();
        }
    }
    
    /**
     * Nested map using blank nodes. Works here because model is local, might not work for remote queries.
     */
    @Test
    public void testNestedBlank() {
        Model data = RDFDataMgr.loadModel("src/test/data/writer/streamtest2b.ttl");
        EndpointSpec ep = EndpointSpecFactory.read(api, "src/test/data/writer/streamtest2.yaml");
        String query = ep.getQuery(new RequestParameters("http://dummy"));
        QueryExecution qexec = QueryExecutionFactory.create(query, data);
        try {
            ValueStream stream = new ValueStream(qexec.execSelect(), ep.getMap());
            assertTrue(stream.hasNext());
            
            ValueSet result = stream.next();
            checkSingleString(result, "label", "resource 1");
            ValueSet nest = (ValueSet)result.getKeyValues("value").getValue();
            checkSingleString(nest, "nlabel", "nest 1");
            
            result = stream.next();
            checkSingleString(result, "label", "resource 2");
            List<Object> nested = result.getKeyValues("value").values();
            assertEquals(2, nested.size());
            ValueSet nested1 = (ValueSet) nested.get(0);
            ValueSet nested2 = (ValueSet) nested.get(1);
            assertTrue( getLex(nested1, "nlabel").equals("nest 2") || getLex(nested2, "nlabel").equals("nest 2") ); 
            assertTrue( getLex(nested1, "nlabel").equals("nest 3") || getLex(nested2, "nlabel").equals("nest 3") ); 
            
            result = stream.next();
            checkSingleString(result, "label", "resource 3");
            assertNull( result.getKeyValues("value") );
            
            assertFalse( stream.hasNext());
        } finally {
            qexec.close();
        }
    }

    @Test
    public void testValueSetFromResource() {
        Model data = RDFDataMgr.loadModel("src/test/data/writer/streamtest2b.ttl");
        Resource root = data.getResource("http://localhost/resource2");
        ValueSet result = ValueSet.fromResource(new JSONMap(api), root);
        assertEquals("http://localhost/resource2", result.getStringID());
        checkSingleString(result, "label", "resource 2");
        List<Object> nested = result.getKeyValues("value").values();
        assertEquals(2, nested.size());
        ValueSet nested1 = (ValueSet) nested.get(0);
        ValueSet nested2 = (ValueSet) nested.get(1);
        assertTrue( getLex(nested1, "label").equals("nest 2") || getLex(nested2, "label").equals("nest 2") ); 
        assertTrue( getLex(nested1, "label").equals("nest 3") || getLex(nested2, "label").equals("nest 3") ); 
    }
    
    private boolean valueIs(Object value, String id, String nlabel, String nvalue) {
        assertTrue(value instanceof ValueSet);
        ValueSet vs = (ValueSet) value;
        if ( ! vs.getStringID().equals(id) ) return false;
        if ( ! nlabel.equals(getLex(vs, "nlabel")) ) return false;
        if ( ! nvalue.equals(getLex(vs, "nvalue")) ) return false;
        return true;
    }
    
    private boolean valueIs3(Object value, String id, String label) {
        assertTrue(value instanceof ValueSet);
        ValueSet vs = (ValueSet) value;
        if ( ! vs.getStringID().equals(id) ) return false;
        if ( ! label.equals(getLex(vs, "nnlabel")) ) return false;
        return true;
    }
    
    private String getLex(ValueSet result, String key) {
        return ((RDFNode)result.getKeyValues(key).getValue()).asLiteral().getLexicalForm();
    }
    
    private void checkSingleString(ValueSet result, String key, String expected) {
        assertEquals( expected,  getLex(result, key));
    }
    
    private void checkStrings(ValueSet result, String key, String...expected) {
        RDFNode[] expectedNodes = new RDFNode[expected.length];
        for (int i = 0; i < expected.length; i++) {
            expectedNodes[i] = createPlainLiteral(expected[i]);
        }
        TestUtil.testArray(result.getKeyValues(key).getValues(), expectedNodes);
    }
}
