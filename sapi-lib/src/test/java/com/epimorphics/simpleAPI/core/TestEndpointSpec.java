/******************************************************************
 * File:        TestEndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  9 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.epimorphics.appbase.core.App;
import com.epimorphics.vocabs.SKOS;
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
}
