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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

import com.epimorphics.util.PrefixUtils;
import com.epimorphics.util.TestUtil;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class TestStreamCoalesce {
    
    @Test
    public void streamWrapperTest() {
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
    
    private void checkSingleString(ValueSet result, String key, String expected) {
        assertEquals( expected, result.getKeyValues(key).getValue().asLiteral().getLexicalForm() );
    }
    
    private void checkStrings(ValueSet result, String key, String...expected) {
        RDFNode[] expectedNodes = new RDFNode[expected.length];
        for (int i = 0; i < expected.length; i++) {
            expectedNodes[i] = createPlainLiteral(expected[i]);
        }
        TestUtil.testArray(result.getKeyValues(key).getValues(), expectedNodes);
    }
}
