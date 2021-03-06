/******************************************************************
 * File:        EndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  30 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.endpoints;

import java.util.Map;

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
     * Finalize a query builder by running the query processors
     * configured for this API (e.g. applying limits, generic filters, geoqueries etc)
     * against the given request parameters (some of which may have been consumed by earlier processing)
     */
    public QueryBuilder finalizeQueryBuilder( QueryBuilder builder, Request request );
    
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
     * Return a comma-separated list of paths that, if multi-valued, should
     * force duplication of records in flat notations such as CSV
     */
    public String getFlattenPath();
    
    /**
     * Return true if flat notations like CSV should hide the @id of the root resource
     */
    public boolean isSuppressID();
    
    /**
     * Return any aliases for query parameters (e.g. to shorten complex path filters)
     */
    public Map<String, String> getAliases();
    
    /**
     * Return default parameter bindings for this endpoint that will be injected into the query
     * as if passed as query/path callparameters but will be overriden by any actual call parameters.
     */
    public Map<String, String> getBindings();
}
