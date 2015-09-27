/******************************************************************
 * File:        TestNan.java
 * Created by:  Dave Reynolds
 * Created on:  23 Feb 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.simpleAPI.attic.core.API;
import com.epimorphics.simpleAPI.attic.core.EndpointSpec;
import com.epimorphics.simpleAPI.attic.writers.JsonWriterUtil;
import com.epimorphics.simpleAPI.attic.writers.ValueSet;
import com.epimorphics.simpleAPI.util.JsonComparator;
import org.apache.jena.rdf.model.Model;

/**
 * Test handling of NaN in numeric values
 */
public class TestNan {

    @Test
    public void testJSONSerialize() {
        EndpointSpec config = new API().getDefaultDescribe();
        
        Model source = RDFDataMgr.loadModel("src/test/data/writer/test-nan.ttl");
        ValueSet values = ValueSet.fromResource(config.getMap(), source.createResource("http://environment.data.gov.uk/flood-monitoring/data/readings/531118-level-stage-i-15_min-m/2015-02-22T06-15-00Z"));
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JSFullWriter out = new JSFullWriter(bos);
        out.startOutput();
        JsonWriterUtil.writeValueSet(config.getMap(), values, out);
        out.finishOutput();
        
        JsonObject jo = JSON.parse(bos.toString());
        System.out.println( jo.toString() );
        JsonObject expected = JSON.read("src/test/data/writer/test-nan.json");
        assertTrue( JsonComparator.equal(expected, jo));

    }

}
