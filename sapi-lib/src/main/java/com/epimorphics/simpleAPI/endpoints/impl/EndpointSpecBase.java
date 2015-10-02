/******************************************************************
 * File:        EndpointSpecBase.java
 * Created by:  Dave Reynolds
 * Created on:  2 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.endpoints.impl;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.ConfigItem;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.requests.Request;
import com.epimorphics.simpleAPI.requests.RequestProcessor;
import com.epimorphics.simpleAPI.views.ViewMap;

/**
 * Provides some useful common methods for implementing EndpointSpecs
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public abstract class EndpointSpecBase extends ConfigItem implements EndpointSpec {
    protected API api;
    protected String url;
    protected ViewMap view;

    public EndpointSpecBase(API api) {
        this.api = api;
    }
    
    /**
     * Return the API instance this endpoint is associated with
     */
    @Override
    public API getAPI() {
        return api;
    }
    
    /**
     * Return the URL pattern for this endpoint, relative to the base of the API.
     * May be null if the endpoint is configured by code
     */
    @Override
    public String getURL() {
        return url;
    }
    
    /**
     * Return the view, if any, which controls formating of query results
     */
    @Override
    public ViewMap getView() {
        return view;
    }

    
    @Override
    public QueryBuilder getQueryBuilder(Request request) {
        QueryBuilder builder = getQueryBuilder();
        for (RequestProcessor proc : api.getRequestProcessors()) {
            builder = proc.process(request, builder, this);
        }
        return builder;
    }

    public void setApi(API api) {
        this.api = api;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setView(ViewMap view) {
        this.view = view;
    }

}
