/******************************************************************
 * File:        SelectEndpointSpecImpl.java
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
import com.epimorphics.simpleAPI.core.RequestParameters;
import com.epimorphics.simpleAPI.core.SelectEndpointSpec;
import com.epimorphics.simpleAPI.writers.JsonWriterUtil;
import com.epimorphics.simpleAPI.writers.KeyValueSetStream;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.query.ResultSet;

public class SelectEndpointSpecImpl extends EndpointSpecBase implements SelectEndpointSpec {
    protected String baseQuery;
    protected String query;
    
    public SelectEndpointSpecImpl(API api, JsonObject config) {
        super(api, config);
    }

    public void setBaseQuery(String query) {
        this.baseQuery = query;
    }
    
    @Override
    public String getQuery(RequestParameters request) {
        if (query == null) {
            if (rawQuery == null) {
                if (map instanceof JSONExplicitMap) {
                    rawQuery = ((JSONExplicitMap)map).asQuery(baseQuery);
                } else {
                    throw new EpiException("Cannot query - need either an explicit mapping or an explicit query");
                }
            }
            query = expandPrefixes( rawQuery );
        }
        return request.bindQueryParams(query);
    }
    
    @Override
    public JSONWritable getWriter(KeyValueSetStream results) {
        return new Writer(results);
    }

    @Override
    public JSONWritable getWriter(ResultSet results) {
        return new Writer(results);
    }
    
    public class Writer implements JSONWritable {
        KeyValueSetStream values;
        
        public Writer(KeyValueSetStream values) {
            this.values = values;
        }
        
        public Writer(ResultSet results) {
            this.values = new KeyValueSetStream(results);
        }
        
        @Override
        public void writeTo(JSFullWriter out) {
            out.startObject();
            api.writeMetadata(out);
            out.key( getItemName() );
            out.startArray();
            while (values.hasNext()) {
                out.arrayElementProcess();
                JsonWriterUtil.writeKeyValues(map, values.next(), out);
            }
            out.finishArray();
            out.finishObject();
        }

    }    

}
