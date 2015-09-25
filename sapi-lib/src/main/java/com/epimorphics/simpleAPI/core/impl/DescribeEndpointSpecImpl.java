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
import com.epimorphics.simpleAPI.core.JSONMap;
import com.epimorphics.simpleAPI.core.RequestParameters;
import com.epimorphics.simpleAPI.writers.JsonWriterUtil;
import com.epimorphics.simpleAPI.writers.ValueSet;
import org.apache.jena.rdf.model.Resource;

public class DescribeEndpointSpecImpl extends EndpointSpecBase implements DescribeEndpointSpec {
    protected String query;
    
    public DescribeEndpointSpecImpl(API api, JsonObject config) {
        super(api, config);
        map = new JSONMap(api);
    }

    @Override
    public String getQuery(RequestParameters request) {
        return request.bindQueryAndID( getQuery() );
    }
    
    @Override
    public String getQuery() {
        if (query == null) {
            query = expandPrefixes( rawQuery );
        }
        return query;
    }

    @Override
    public JSONWritable getWriter(Resource resource, String requestURI) {
        return new Writer(resource, requestURI);
    }

    public class Writer implements JSONWritable {
        ValueSet values;
        String requestURI;
        
        public Writer(ValueSet values, String requestURI) {
            this.values = values;
            this.requestURI = requestURI;
        }
        
        public Writer(Resource root, String requestURI) {
            this.values = ValueSet.fromResource(map, root);
            this.requestURI = requestURI;
        }
        
        @Override
        public void writeTo(JSFullWriter out) {
            out.startObject();
            writeMetadata(out);
            out.key( getItemName() );
            JsonWriterUtil.writeValueSet(map, values, out);
            out.finishObject();
        }
        
        protected void writeMetadata(JSFullWriter out) {
            api.startMetadata(out);
            writeFormats(requestURI, out);
            api.finishMetadata(out);
        }
        
    }
}
