/******************************************************************
 * File:        Call.java
 * Created by:  Dave Reynolds
 * Created on:  6 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import javax.ws.rs.NotFoundException;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.ItemQuery;
import com.epimorphics.simpleAPI.query.ListQuery;
import com.epimorphics.simpleAPI.query.Query;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.results.ResultOrStream;

/**
 * Represents all the information involved in invoking a single API call.
 * Includes the API configuration, the specification of the selected endpoint
 * and all the parameters from the REST request.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Call {
    protected EndpointSpec endpoint;
    protected Request request;
    protected String templateName;
    
    public Call(EndpointSpec endpoint, Request request) {
        this.endpoint = endpoint;
        this.request = request;
    }
    
    public Call(API api, String endpointName, Request request) {
        endpoint = api.getSpec(endpointName);
        if (endpoint == null) {
            throw new NotFoundException("Could not locate endpoint specification: " + endpointName);
        }
        this.request = request;
    }
    
    public API getAPI() {
        return endpoint.getAPI();
    }
    
    public EndpointSpec getEndpoint() {
        return endpoint;
    }
    
    public Request getRequest() {
        return request;
    }
    
    /**
     * Return a query builder corresponding to this call.
     */
    public QueryBuilder getQueryBuilder() {
        return endpoint.getQueryBuilder(request);
    }
    
    /**
     * Return the results for this call, it builds the query and 
     * runs it on the configured data source. Suitable for simple
     * cases where no custom processing of request or query is needed.
     */
    public ResultOrStream getResults() {
        Query query = getQueryBuilder().build();
        if (query instanceof ListQuery) {
            return getAPI().getSource().query((ListQuery)query, this);
        } else {
            return getAPI().getSource().query((ItemQuery)query, this);
        }
    }
    
    /**
     * Return the name of a (velocity or other) template to use for HTML rendering of this endpoint
     */
    public String getTemplateName() {
        return templateName == null ? endpoint.getTemplateName() : templateName;
    }

    /**
     * Override the template to use for HTML render to this call.
     * Used for custom construction of endpoints
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    
    
}
