/******************************************************************
 * File:        TestEndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  9 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.simpleAPI.core.impl.JSONMapEntry;
import com.epimorphics.simpleAPI.core.impl.ListEndpointSpecImpl;
import com.epimorphics.vocabs.SKOS;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Test some of the shared implementation parts of endpoint specs
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TestEndpointSpec {
    App app;
    API api;
    
    @Before
    public void setUP() throws IOException {
        app = new App("test", new File("src/test/TestApp/WEB-INF/static-app.conf"));
        api = app.getA(API.class);
    }

    @Test
    public void testPrefixes() {
        EndpointSpec endpoint = EndpointSpecFactory.read(api, "src/test/TestApp/WEB-INF/endpoints/alertTestExplicitQuery.yaml");
        
        PrefixMapping prefixes = endpoint.getPrefixes();
        assertEquals( "http://environment.data.gov.uk/flood-monitoring/def/core/", prefixes.getNsPrefixURI("rt") );
        assertEquals( SKOS.getURI(), prefixes.getNsPrefixURI("skos") );
        
        endpoint = EndpointSpecFactory.read(api, "src/test/data/endpointSpecs/describe-test.yaml");
        
        String query = endpoint.getQuery( new RequestParameters("http://localhost/") );
        assertTrue( query.contains("PREFIX rt: <http://environment.data.gov.uk/flood-monitoring/def/core/>"));
        assertTrue( query.contains("DESCRIBE <http://localhost/> ?warning"));
        assertTrue( query.contains("OPTIONAL { <http://localhost/> rt:currentWarning ?warning }"));
    }
    
    @Test
    public void testQueryGeneration() {
        EndpointSpec endpoint = EndpointSpecFactory.read(api, "src/test/data/endpointSpecs/queryBuildTest.yaml");
        String query = endpoint.getQuery( new RequestParameters("http://localhost/") );
        
        assertTrue( query.contains("PREFIX dct: <http://purl.org/dc/terms/>") );
        assertTrue( query.contains("PREFIX rt: <http://environment.data.gov.uk/flood-monitoring/def/core/>") );
        assertTrue( query.contains("PREFIX skos: <http://www.w3.org/2004/02/skos/core#>") );
        assertTrue( query.contains("SELECT * WHERE {") );
        assertTrue( query.contains("?id a rt:FloodAlertOrWarning .") );

        assertTrue( query.contains("rt:severity ?severity ;") );
        assertTrue( query.contains("rt:severityLevel ?severityLevel ;") );
        assertTrue( query.contains("rt:floodArea ?floodArea ;") );
        assertTrue( query.contains("rt:eaAreaName ?eaAreaName ;") );
        assertTrue( query.contains("dct:description ?description") );
        assertTrue( query.contains("OPTIONAL {?id rt:message ?message .}") );
        assertTrue( query.contains("?floodArea") );
        assertTrue( query.contains("skos:notation ?notation ;") );
        assertTrue( query.contains("rt:county ?county ;") );
        
        boolean ok = true;
        try { QueryFactory.create(query); } catch (Exception e) { ok = false; }
        assertTrue( ok );
    }
    

    static final String[] expected = new String[]{
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n",
        "SELECT * WHERE {\n",
        "?id a rdfs:Class .\n",
        "?id\n",
        "<http://www.w3.org/1999/02/22-rdf-syntax-ns#value> ?value ;\n",
        "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?mytype ;\n",
        "OPTIONAL {?id <http://www.w3.org/2000/01/rdf-schema#label> ?label .}\n",
        "OPTIONAL {?id rdfs:comment ?comment .}\n",
        "}\n"};

    static final String[] expectedNested = new String[] {
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
        "PREFIX ex: <http://localhost/>",
        "SELECT * WHERE {",
        "?id a ex:Root .",
        "?id",
        "rdfs:label ?label ;",
        "ex:first ?first ;",
        "ex:second ?second ;",
        "?first",
        "rdfs:label ?first_label ;",
        "OPTIONAL {?first ex:child ?first_child .}",
        "?second",
        "rdfs:label ?second_label ;",
        "OPTIONAL {?second ex:child ?second_child .}"
    };
    
    static final String[] expectedNBug = new String[] {
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
        "PREFIX ex: <http://localhost/>",
        "SELECT * WHERE {",
        "?id a ex:Root .",
        "?id",
        "rdfs:label ?label ;",
        "OPTIONAL {?id ex:warning ?warning .",
        "?warning",
        "ex:description ?description ;",
        "ex:severity ?severity ;"
    };
    
    
    @Test
    public void testParse() {
        EndpointSpec endpoint = EndpointSpecFactory.read(api, "src/test/data/writer/testspec.yaml");
        checkTestQuery(endpoint, expected);
    }
    
    @Test
    public void testNestedParse() {
        EndpointSpec endpoint = EndpointSpecFactory.read(api, "src/test/data/writer/testnestedspec.yaml");
        checkTestQuery(endpoint, expectedNested);
    }
    
    @Test
    public void testNestedBug() {
        EndpointSpec endpoint = EndpointSpecFactory.read(api, "src/test/data/writer/testNestedBug.yaml");
        checkTestQuery(endpoint, expectedNBug);
    }
    
    private void checkTestQuery(EndpointSpec endpoint, String[] expect) {
        String query = endpoint.getQuery( new RequestParameters("http://localhost/") );
        for (String e : expect) {
            assertTrue( query.contains(e) );
        }
    }
     
    @Test
    public void testFilterInject() {
        EndpointSpec endpoint = EndpointSpecFactory.read(api, "src/test/data/endpointSpecs/queryBuildTest.yaml");
        
        RequestParameters request = new RequestParameters("http://localhost/");
        request.addParameter("severityLevel", "2");
        String query = endpoint.getQuery( request );
        if (ListEndpointSpecImpl.USE_FILTER) {
            assertTrue( query.contains(" FILTER( ?severityLevel = \"2\"^^<http://www.w3.org/2001/XMLSchema#int> )") );
        } else {
            assertTrue( query.contains(" VALUES ?severityLevel { \"2\"^^<http://www.w3.org/2001/XMLSchema#int> }") );
        }
        
        request = new RequestParameters("http://localhost/");
        request.addParameter("severity", "Alert");
        query = endpoint.getQuery( request );
        if (ListEndpointSpecImpl.USE_FILTER) {
            assertTrue( query.contains(" FILTER( ?severity = \"Alert\" )") );
        } else {
            assertTrue( query.contains("VALUES ?severity { \"Alert\" }") );
        }
        
        request = new RequestParameters("http://localhost/");
        request.addParameter("floodArea", "http://example.com/test");
        query = endpoint.getQuery( request );
        if (ListEndpointSpecImpl.USE_FILTER) {
            assertTrue( query.contains("FILTER( ?floodArea = <http://example.com/test> )") );
        } else {
            assertTrue( query.contains("VALUES ?floodArea { <http://example.com/test> }") );
        }
        
        request = new RequestParameters("http://localhost/");
        request.addParameter("county", "Kent");
        query = endpoint.getQuery( request );
        if (ListEndpointSpecImpl.USE_FILTER) {
            assertTrue( query.contains(" FILTER( ?county = \"Kent\" )") );
        } else {
            assertTrue( query.contains("VALUES ?county { \"Kent\" }") );
        }
        
    }
    
    @Test
    public void testLimit() {
        checkLimit("8", "3", null, "LIMIT 8 OFFSET 3");
        checkLimit("8", "3", 5, "LIMIT 5 OFFSET 3");
        checkLimit("20", "3", null, "LIMIT 10 OFFSET 3");
    }
    
    private void checkLimit(String limit, String offset, Integer maxLimit, String expect) {
        EndpointSpec endpoint = EndpointSpecFactory.read(api, "src/test/data/endpointSpecs/queryBuildTest.yaml");
        
        RequestParameters request = new RequestParameters("http://localhost/");
        if (maxLimit != null) {
            request.setLimit(maxLimit);
        }
        request.addParameter("_limit", limit);
        request.addParameter("_offset", offset);
        String query = endpoint.getQuery( request );
        
        assertTrue( query.contains(expect) );
    }
    
    @Test
    public void testParseBug() {
        EndpointSpec endpoint = EndpointSpecFactory.read(api, "src/test/TestApp/WEB-INF/endpoints/alertTestExplicitQuery.yaml");
        
        JSONMap map = endpoint.getMap();
        JSONNodeDescription polygonmap = map.getEntry("polygon");
        assertTrue( polygonmap instanceof JSONMapEntry);
        assertTrue( polygonmap.isChild() );
    }
}
