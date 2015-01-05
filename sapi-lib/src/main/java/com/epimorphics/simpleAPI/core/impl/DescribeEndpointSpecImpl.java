/******************************************************************
 * File:        DescribeEndpointSpecImpl.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core.impl;

import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.DescribeEndpointSpec;
import com.epimorphics.simpleAPI.core.RequestParameters;
import com.epimorphics.simpleAPI.writers.JsonWriterUtil;
import com.epimorphics.simpleAPI.writers.KeyValueSet;
import com.hp.hpl.jena.rdf.model.Resource;

public class DescribeEndpointSpecImpl extends EndpointSpecBase implements DescribeEndpointSpec {
    protected String query = "DESCRIBE ?id";

    public DescribeEndpointSpecImpl(API api, JsonObject config) {
        super(api, config);
    }
    
    public void setQueryTemplate(String query) {
        this.query = query;
    }

    @Override
    public String getQuery(RequestParameters request) {
        return request.bindQuery(query);
    }

    @Override
    public JSONWritable getWriter(Resource resource) {
        return new Writer(resource);
    }

    public class Writer implements JSONWritable {
        KeyValueSet values;
        
        public Writer(KeyValueSet values) {
            this.values = values;
        }
        
        public Writer(Resource root) {
            this.values = KeyValueSet.fromResource(DescribeEndpointSpecImpl.this, root);
        }
        
        @Override
        public void writeTo(JSFullWriter out) {
            out.startObject();
            api.writeMetadata(out);
            out.key( getItemName() );
            JsonWriterUtil.writeKeyValues(DescribeEndpointSpecImpl.this, values, out);
            out.finishObject();
        }

    }
}
