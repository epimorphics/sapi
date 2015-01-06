/******************************************************************
 * File:        EndpointSpecBase.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core.impl;

import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.json.JsonUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.EndpointSpec;
import com.epimorphics.simpleAPI.core.EndpointSpecFactory;
import com.epimorphics.simpleAPI.core.JSONMap;
import com.epimorphics.simpleAPI.core.RequestParameters;

/**
 * Base implementation for EndpointSpecs.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public abstract class EndpointSpecBase implements EndpointSpec {
    protected API api;
    protected JsonObject config;
    protected JSONMap map;
    protected String query;   // Optional, query may be constructed from mapping specification

    public EndpointSpecBase(API api, JsonObject config) {
        this.api = api;
        this.config = config;
    }
    
    public void setQueryTemplate(String query) {
        this.query = query;
    }
    
    @Override
    public API getAPI() {
        return api;
    }

    @Override
    public JsonObject getMetadata() {
        return config;
    }

    @Override
    public JSONMap getMap() {
        return map;
    }
    
    @Override
    public abstract String getQuery(RequestParameters request);

    public String getItemName() {
        return JsonUtil.getStringValue(config, EndpointSpecFactory.ITEM_NAME, "item");
    }

    public String getName() {
        return JsonUtil.getStringValue(config, EndpointSpecFactory.NAME);
    }

    public void setMapping(JSONMap map) {
        this.map = map;
    }
       
}
