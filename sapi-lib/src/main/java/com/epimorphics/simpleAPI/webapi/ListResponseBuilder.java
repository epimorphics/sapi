/******************************************************************
 * File:        ListRepsonseBuilder.java
 * Created by:  Dave Reynolds
 * Created on:  9 Feb 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi;

import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.data.impl.WResultSetWrapper;
import com.epimorphics.simpleAPI.core.ListEndpointSpec;
import com.epimorphics.simpleAPI.core.RequestParameters;
import com.hp.hpl.jena.query.ResultSet;

public class ListResponseBuilder extends EPResponseBuilder {
    static final Logger log = LoggerFactory.getLogger( ListResponseBuilder.class );

    protected ListEndpointSpec spec;
    protected RequestParameters params;
    protected ResultSet results;

    public ListResponseBuilder(ServletContext context, UriInfo uriInfo) {
        super(context, uriInfo);
    }

    /**
     * list items based on a named query/mapping endpoint specification
     */
    public ListResponseBuilder list(String specname) {
        return list(specname, getRequestWithParms());
    }

    /**
     * list items based on a named query/mapping endpoint specification, 
     * passing in a pre-built set of request parameters
     */
    public ListResponseBuilder list(String specname, RequestParameters params) {
        this.spec = getAPI().getSelectSpec(specname);
        this.params = params;
        return this;
    }
    
    /**
     * format the given query results according to the endpoint specification
     */
    public ListResponseBuilder list(String specname, RequestParameters params, ResultSet results) {
        return list(getAPI().getSelectSpec(specname), params, results);
    }
    
    /**
     * format the given query results according to the endpoint specification
     */
    public ListResponseBuilder list(ListEndpointSpec spec, RequestParameters params, ResultSet results) {
        this.spec = spec;
        this.params = params;
        this.results = results;
        return this;
    }
    
    @Override
    public Object getEntity() {
        if (results == null) {
            String query = spec.getQuery(params);
            log.debug( "List query = " + query);
            results = getSource().streamableSelect(query);            
        }
        switch (format) {
        case json:
            return spec.getWriter(results, params);
        case csv:
            return spec.getCSVWriter(results, params, csvIncludeID);
        case rdf:
            // TODO implement
            throw new WebApplicationException(Status.NOT_ACCEPTABLE);
        case html:
            return new WResultSetWrapper(results, getWSource());
        default:
            // can't happen
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String getEntityVelocityName() {
        return "results";
    }

}
