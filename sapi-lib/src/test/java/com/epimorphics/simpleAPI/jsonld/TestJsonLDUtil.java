/******************************************************************
 * File:        TestJsonLDUtil.java
 * Created by:  Dave Reynolds
 * Created on:  13 Jan 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.jsonld;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class TestJsonLDUtil {
    protected static final String TESTDATA = "src/test/data/jsonld/";
    
    @Test
    public void testBasicParse() throws IOException {
        Object context = JsonLDUtil.readContextFromFile(TESTDATA + "context.json");
        assertNotNull( context );
        assertTrue( context instanceof Map<?, ?> );
        Model model = JsonLDUtil.readModel("http://example.com/", new FileInputStream(TESTDATA + "data.json"), context);
        Model expected = RDFDataMgr.loadModel( TESTDATA + "expected.ttl" );
        assertTrue( model.isIsomorphicWith(expected) );
    }
}
