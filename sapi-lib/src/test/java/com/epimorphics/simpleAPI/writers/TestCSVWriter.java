/******************************************************************
 * File:        TestCSVWriter.java
 * Created by:  Dave Reynolds
 * Created on:  8 Feb 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;
import static org.junit.Assert.*;

import com.epimorphics.util.PrefixUtils;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;

public class TestCSVWriter {

    @Test
    public void testBasicWrite() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CSVWriter writer = new CSVWriter(bos);
        writer.write( streamFrom("src/test/data/writer/csvtest.ttl", "SELECT * WHERE { ?id rdf:value ?value; rdfs:label ?label} ORDER BY ?value") );
        String expected = "@id,label,value\r\n" + "http://localhost/one,foo,1\r\n" + "http://localhost/two,bar|baz,2\r\n";
        assertEquals(expected, bos.toString());
    }
    
    protected ValueStream streamFrom(String file, String query) {
        Model model = RDFDataMgr.loadModel(file);
        query = PrefixUtils.expandQuery(query, model);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        try {
            ResultSet rs = qexec.execSelect();
            return new ValueStream( ResultSetFactory.makeRewindable(rs) );
        } finally {
            qexec.close();
        }
    }
}