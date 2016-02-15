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
    public static final String DEFAULT_VIEWNAME = "default";

    /**
     * Return the API instance this endpoint is associated with
     */
    public API getAPI();
    
    /**
     * Return a generic query builder for this endpoint, using the default View
     */
    public QueryBuilder getQueryBuilder();
    
    /**
     * Return a generic query builder for this endpoint, using the named View
     */
    public QueryBuilder getQueryBuilder(String name);
    
    /**
     * Return a query builder for this endpoint, as customized by
     * the given request parameters
     */
    public QueryBuilder getQueryBuilder(Request request);
    
    /**
     * Return the default view, if any, which controls formating of query results
     */
    public ViewMap getView();
    
    /**
     * Return a named view which controls formating of query results
     */
    public ViewMap getView(String viewname);
    
    /**
     * Return the URL pattern for this endpoint, relative to the base of the API.
     * May be null if the endpoint is configured by code
     */
    public String getURL() ;
    
    
    /**
     * Return the prefix bindings defined for this Endpoint (including the App-wide prefixes)
     */
    public PrefixMapping getPrefixes();
    
    /**
     * Return the name of a (velocity or other) template to use for HTML rendering of this endpoint
     */
    public String getTemplateName();

    /**
     * Return a path that, if multi-valued, should
     * force duplication of records in flat notations such as CSV
     */
    public String getFlattenPath();
}
