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

import com.epimorphics.json.JSONWritable;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.RequestParameters;
import com.epimorphics.simpleAPI.core.SelectEndpointSpec;
import com.epimorphics.simpleAPI.writers.KeyValueSetStream;

public class SelectEndpointSpecImpl extends EndpointSpecBase implements SelectEndpointSpec {

    public SelectEndpointSpecImpl(API api, JsonObject config) {
        super(api, config);
    }

    @Override
    public String getQuery(RequestParameters request) {
        if (query == null) {
            // TODO construct implicit query from JSON mapping
        }
        return request.bindQuery(query);
    }

    @Override
    public JSONWritable getWriter(KeyValueSetStream results) {
        // TODO Auto-generated method stub
        @@ working here
        return null;
    }

}
