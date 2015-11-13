/******************************************************************
 * File:        TestQueryStrings.java
 * Created by:  Dave Reynolds
 * Created on:  26 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static com.epimorphics.test.utils.Asserts.*;

import java.io.File;
import java.io.IOException;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.shared.PrefixMapping;
import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.vocabs.SKOS;
import com.epimorphics.webapi.test.MockUriInfo;

/**
 * Mimimal tests of string bashing, probably will be replaced.
 *  
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TestQueryStrings {
    App app;
    API api;
    
    @Before
    public void setUP() throws IOException {
        app = new App("test", new File("src/test/testCases/testQueries/WEB-INF/app.conf"));
        api = app.getA(API.class);
        app.startup();
    }

    @Test
    public void testPrefixes() {
        EndpointSpec endpoint = api.getSpec("alertTestExplicitQuery");
        
        PrefixMapping prefixes = endpoint.getPrefixes();
        assertEquals( "http://environment.data.gov.uk/flood-monitoring/def/core/", prefixes.getNsPrefixURI("rt") );
        assertEquals( SKOS.getURI(), prefixes.getNsPrefixURI("skos") );
        
        String query = api.getCall("describe-test", new MockUriInfo("test"), null).getQueryBuilder().build().toString();
        assertContains( query, "PREFIX rt: <http://environment.data.gov.uk/flood-monitoring/def/core/>");
        assertContains( query, "DESCRIBE <http://localhost/flood-monitoring/test> ?warning");
        assertContains( query, "OPTIONAL { <http://localhost/flood-monitoring/test> rt:currentWarning ?warning }");
    }
    
    @Test
    public void testQueryGeneration() {
        String query = api.getCall("queryBuildTest", new MockUriInfo("test"), null).getQueryBuilder().build().toString();
        
        assertContains( query, "PREFIX rt: <http://environment.data.gov.uk/flood-monitoring/def/core/>" );
        assertContains( query, "SELECT * WHERE {" );
        assertContains( query, "?id a rt:FloodAlertOrWarning ." );
        
        assertContains( query, "<http://environment.data.gov.uk/flood-monitoring/def/core/severity> ?severity ;" );
        assertContains( query, "<http://environment.data.gov.uk/flood-monitoring/def/core/severityLevel> ?severityLevel ;" );
        assertContains( query, "<http://environment.data.gov.uk/flood-monitoring/def/core/floodArea> ?floodArea ;" );
        assertContains( query, "<http://environment.data.gov.uk/flood-monitoring/def/core/eaAreaName> ?eaAreaName ;" );
        assertContains( query, "<http://purl.org/dc/terms/description> ?description ;" );
        assertContains( query, "OPTIONAL {?id <http://environment.data.gov.uk/flood-monitoring/def/core/message> ?message .}" );
        assertContains( query, "?floodArea" );
        assertContains( query, "<http://www.w3.org/2004/02/skos/core#notation> ?floodArea_notation ;" );
        assertContains( query, "<http://environment.data.gov.uk/flood-monitoring/def/core/county> ?floodArea_county ;" );

        boolean ok = true;
        try { QueryFactory.create(query); } catch (Exception e) { ok = false; }
        assertTrue( ok );
    }
    
}
