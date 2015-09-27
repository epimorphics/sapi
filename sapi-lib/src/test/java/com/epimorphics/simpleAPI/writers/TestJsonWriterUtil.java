/******************************************************************
 * File:        TestJsonWriterUtil.java
 * Created by:  Dave Reynolds
 * Created on:  10 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
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

public class TestJsonWriterUtil {
    
    @Test
    public void testJSONSerialize() {
        EndpointSpec config = new API().getDefaultDescribe();
        
        Model source = RDFDataMgr.loadModel("src/test/data/writer/test1.ttl");
        ValueSet values = ValueSet.fromResource(config.getMap(), source.createResource("http://localhost/resource"));
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JSFullWriter out = new JSFullWriter(bos);
        out.startOutput();
        JsonWriterUtil.writeValueSet(config.getMap(), values, out);
        out.finishOutput();
        
        JsonObject jo = JSON.parse(bos.toString());
        JsonObject expected = JSON.read("src/test/data/writer/test1.json");
        assertTrue( JsonComparator.equal(expected, jo));

    }

}
