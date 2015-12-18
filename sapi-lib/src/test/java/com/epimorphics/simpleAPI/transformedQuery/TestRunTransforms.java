/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.simpleAPI.transformedQuery;

import static com.epimorphics.util.Asserts.assertContains;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.jena.shared.PrefixMapping;
import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.query.impl.SparqlQueryBuilder;
import com.epimorphics.sparql.geo.GeoQuery;
import com.epimorphics.sparql.terms.Var;
import com.epimorphics.vocabs.SKOS;
import com.epimorphics.webapi.test.MockUriInfo;

public class TestRunTransforms {
	
	App app;
	API api;
	    
	@Before	public void setUP() throws IOException {
		app = new App("test", new File("src/test/testCases/runTransforms/WEB-INF/app.conf"));
	    api = app.getA(API.class);
	    app.startup();
	}
	
	@Test public void testSetup() {
	}
	
	@Test public void testEndToEnd() {
        EndpointSpec endpoint = api.getSpec("run-transform-test");
        
        PrefixMapping prefixes = endpoint.getPrefixes();
        assertEquals( "http://environment.data.gov.uk/flood-monitoring/def/core/", prefixes.getNsPrefixURI("rt") );
        assertEquals( SKOS.getURI(), prefixes.getNsPrefixURI("skos") );
        
        GeoQuery gq = new GeoQuery(new Var("id"), "withinCircle", 60.1, 19.2, 11.0);
        QueryBuilder baseQB = api.getCall("run-transform-test", new MockUriInfo("test"), null).getQueryBuilder();
        QueryBuilder geoQB = ((SparqlQueryBuilder) baseQB).geoQuery(gq);
		String query = geoQB.build().toString();
                
        System.err.println(">> query:\n" + query);
        assertContains( query, "?id <http://jena.apache.org/spatial#withinCircle> (60.1 19.2 11.0) ." );
        
//        assertContains( query, "PREFIX rt: <http://environment.data.gov.uk/flood-monitoring/def/core/>");
//        assertContains( query, "DESCRIBE <http://localhost/flood-monitoring/test> ?warning");
//        assertContains( query, "OPTIONAL { <http://localhost/flood-monitoring/test> rt:currentWarning ?warning }");
    }
}
