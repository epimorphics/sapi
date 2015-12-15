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
import com.epimorphics.vocabs.SKOS;
import com.epimorphics.webapi.test.MockUriInfo;

public class TestTransformations {
	
	App app;
	API api;
	    
	@Before	public void setUP() throws IOException {
		app = new App("test", new File("src/test/testCases/testTransforms/WEB-INF/app.conf"));
	    api = app.getA(API.class);
	    app.startup();
	}
	
	@Test public void testSetup() {
	}
	
	@Test public void testA() {
        EndpointSpec endpoint = api.getSpec("describe-test");
        
        PrefixMapping prefixes = endpoint.getPrefixes();
        assertEquals( "http://environment.data.gov.uk/flood-monitoring/def/core/", prefixes.getNsPrefixURI("rt") );
        assertEquals( SKOS.getURI(), prefixes.getNsPrefixURI("skos") );
        
        String query = api.getCall("describe-test", new MockUriInfo("test"), null).getQueryBuilder().build().toString();
                
        System.err.println(">> query:\n" + query);
        assertContains( query, "?id <http://jena.apache.org/spatial#withinCircle> (60.1 19.2 11.0) ." );
        
//        assertContains( query, "PREFIX rt: <http://environment.data.gov.uk/flood-monitoring/def/core/>");
//        assertContains( query, "DESCRIBE <http://localhost/flood-monitoring/test> ?warning");
//        assertContains( query, "OPTIONAL { <http://localhost/flood-monitoring/test> rt:currentWarning ?warning }");
    }
}
