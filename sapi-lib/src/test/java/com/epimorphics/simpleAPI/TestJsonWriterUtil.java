/******************************************************************
 * File:        TestJsonWriterUtil.java
 * Created by:  Dave Reynolds
 * Created on:  10 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI;

import java.io.ByteArrayOutputStream;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;
import static org.junit.Assert.*;

import com.epimorphics.json.JSFullWriter;
import com.hp.hpl.jena.rdf.model.Model;

public class TestJsonWriterUtil {
    
    @Test
    public void testJSONSerialize() {
        APIConfig config = new API().getDefaultConfig();
        
        Model source = RDFDataMgr.loadModel("src/test/data/writer/test1.ttl");
        KeyValueSet values = KeyValueSet.fromResource(config, source.createResource("http://localhost/resource"));
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JSFullWriter out = new JSFullWriter(bos);
        out.startOutput();
        JsonWriterUtil.writeKeyValues(config, values, out);
        out.finishOutput();
        
        JsonObject jo = JSON.parse(bos.toString());
        JsonObject expected = JSON.read("src/test/data/writer/test1.json");
        assertTrue( JsonComparator.equal(expected, jo));

    }

}
