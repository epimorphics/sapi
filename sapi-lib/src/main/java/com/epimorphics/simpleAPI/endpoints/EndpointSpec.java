/******************************************************************
 * File:        EndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  30 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.endpoints;

import org.apache.jena.shared.PrefixMapping;

import com.epimorphics.appbase.monitor.ConfigInstance;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.requests.Request;
import com.epimorphics.simpleAPI.views.ViewMap;

public interface EndpointSpec extends ConfigInstance {

    /**
     * Return the API instance this endpoint is associated with
     */
    public API getAPI();
    
    /**
     * Return a generic query builder for this endpoint
     */
    public QueryBuilder getQueryBuilder();
    
    /**
     * Return a query builder for this endpoint, as customized by
     * the given request parameters
     */
    public QueryBuilder getQueryBuilder(Request request);
    
    /**
     * Return the view, if any, which controls formating of query results
     */
    public ViewMap getView();
    
    /**
     * Return the URL pattern for this endpoint, relative to the base of the API.
     * May be null if the endpoint is configured by code
     */
    public String getURL() ;
    
    
    /**
     * Return the prefix bindings defined for this Endpoint (including the App-wide prefixes)
     */
    public PrefixMapping getPrefixes();
    
}
