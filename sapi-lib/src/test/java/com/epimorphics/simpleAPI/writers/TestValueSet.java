/******************************************************************
 * File:        TestStreamCoalesce.java
 * Created by:  Dave Reynolds
 * Created on:  10 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonNull;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.epimorphics.appbase.core.App;
import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.EndpointSpec;
import com.epimorphics.simpleAPI.core.EndpointSpecFactory;
import com.epimorphics.simpleAPI.core.JSONMap;
import com.epimorphics.simpleAPI.core.RequestParameters;
import com.epimorphics.simpleAPI.util.JsonComparator;
import com.epimorphics.util.EpiException;
import com.epimorphics.util.PrefixUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

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
    public void testBasicStreamWrapper() throws IOException {
        String query = "SELECT * WHERE {?id rdfs:label ?label; rdf:value ?value. OPTIONAL {?id rdfs:comment ?comment} } ORDER BY ?id";
        runTest("src/test/data/writer/streamtest.ttl", query, null, "src/test/data/writer/expected-streamtest.yaml");
    }
    
    /**
     * Mapping tests
     */
    @Test
    public void testMapEndpoints() throws IOException {
        // Simple nesting
        EndpointSpec ep = EndpointSpecFactory.read(api, "src/test/data/writer/streamtest2.yaml");
        runTest("src/test/data/writer/streamtest2.ttl", ep, "src/test/data/writer/expected-nested.yaml");
        
        // Two deep nested map
        ep = EndpointSpecFactory.read(api, "src/test/data/writer/streamtest3.yaml");
        runTest("src/test/data/writer/streamtest3.ttl", ep, "src/test/data/writer/expected-nested3.yaml");

        // Nested map using blank nodes. Works here because model is local, might not work for remote queries.
        ep = EndpointSpecFactory.read(api, "src/test/data/writer/streamtest2.yaml");
        runTest("src/test/data/writer/streamtest2b.ttl", ep, "src/test/data/writer/expected-nestedBlank.yaml");
    }

    @Test
    public void testValueSetFromResource() {
        // Simple nesting base with blank nodes
        runResourceTest(
                "src/test/data/writer/streamtest2b.ttl", "http://localhost/resource2", 
                "src/test/data/writer/expected-fromResource1.json");
        
        // Deeper nesting cases but still trees
        runResourceTest(
                "src/test/data/writer/testNested1.ttl", "http://localhost/top", 
                "src/test/data/writer/expectedResource-nested1.json"); 
        runResourceTest(
                "src/test/data/writer/testNested3.ttl", "http://localhost/top",  
                "src/test/data/writer/expectedResource-nested3.json"); 
        
        // Circular cases
        runResourceTest(
                "src/test/data/writer/testNested4.ttl", "http://localhost/top",
                "src/test/data/writer/expectedResource-nested4.json"); 
    }
    
    protected void runResourceTest(String dataFile, String rootURI, String expected) {
        Model data = RDFDataMgr.loadModel(dataFile);
        Resource root = data.getResource(rootURI);
        ValueSet result = ValueSet.fromResource(new JSONMap(api), root);
        if (expected == null) {
            System.out.println( asJson(result).toString() );
        } else {
            assertTrue( matches(result, expected) );
        }
    }
    
    protected void runTest(String dataFile, EndpointSpec ep, String expectedStream) throws IOException {
        String query = ep.getQuery( new RequestParameters("http://dummy.com/") );
        runTest(dataFile, query, ep.getMap(), expectedStream);
    }
    
    protected void runTest(String dataFile, String query, JSONMap map, String expectedStream) throws IOException {
        Model data = RDFDataMgr.loadModel(dataFile);
        query = PrefixUtils.expandQuery(query, PrefixUtils.commonPrefixes());
        QueryExecution qexec = QueryExecutionFactory.create(query, data);
        try {
            ValueStream stream = new ValueStream(qexec.execSelect(), map);
            assertTrue( matches(stream, expectedStream) );
        } finally {
            qexec.close();
        }
    }
    
    // Check result stream against yaml file, one "document" per result
    protected boolean matches(ValueStream stream, String expectedFilename) throws IOException {
        List<JsonObject> expected = new ArrayList<JsonObject>();
        for (Object doc : new Yaml().loadAll( new FileInputStream(expectedFilename) )) {
            expected.add( JsonUtil.asJson(doc).getAsObject() );
        }
        for (JsonObject ex : expected) {
            if (!stream.hasNext()) return false;
            if (! JsonComparator.equal(ex, asJson(stream.next())) ) return false;
        }
        if (stream.hasNext()) return false;
        return true;
    }
    
    // Check single result against json file
    protected boolean matches(ValueSet result, String expectedFilename) {
        JsonObject expected = JSON.read(expectedFilename);
        JsonObject actual = asJson(result);
        return JsonComparator.equal(expected, actual);
    }
    
    /**
     * Convert valueset to a approximate JSON representation for test purposes
     */
    public static JsonObject asJson(ValueSet vs) {
        JsonObject result = new JsonObject();
        for (KeyValues kv : vs.listKeyValues()) {
            result.put(kv.getKey(), asJson(kv));
        }
        if (vs.getStringID() != null) {
            result.put("@id", vs.getStringID());
        }
        return result;
    }
    
    public static JsonValue asJson(KeyValues kv) {
        if (kv.getValues().isEmpty()) {
            return JsonNull.instance; 
        } else if (kv.getValues().size() == 1) {
            return asJsonValue(kv.getValue());
        } else {
            JsonArray results = new JsonArray();
            for (Object value : kv.getValues()) {
                results.add( asJsonValue(value) );
            }
            return results;
        }
    }
    
    protected static JsonValue asJsonValue(Object value) {
        if (value instanceof ValueSet) {
            return asJson((ValueSet)value);
        } else if (value instanceof RDFNode) {
            RDFNode n = (RDFNode)value;
            if (n.isLiteral()) {
                return new JsonString( n.asLiteral().getLexicalForm() );
            } else if (n.isURIResource()) {
                return new JsonString( n.asResource().getURI() );
            } else {
                return new JsonString( "[]" );
            }
        } else {
            throw new EpiException("Illegal ValueSet structure");
        }
    }
}
