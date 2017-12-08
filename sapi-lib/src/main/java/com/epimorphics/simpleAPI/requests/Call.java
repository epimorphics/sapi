/******************************************************************
 * File:        Call.java
 * Created by:  Dave Reynolds
 * Created on:  6 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.webapi.ExtensionFilter;
import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.rdfutil.TypeUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.DataSource;
import com.epimorphics.simpleAPI.query.ItemQuery;
import com.epimorphics.simpleAPI.query.ListQuery;
import com.epimorphics.simpleAPI.query.Query;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.results.ResultOrStream;
import com.epimorphics.simpleAPI.views.PropertySpec;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ViewPath;
import com.epimorphics.util.NameUtils;

/**
 * Represents all the information involved in invoking a single API call.
 * Includes the API configuration, the specification of the selected endpoint
 * and all the parameters from the REST request.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class Call {
    static Logger log = LoggerFactory.getLogger( Call.class );
    
    protected EndpointSpec endpoint;
    protected Request request;
    protected String templateName;
    protected QueryBuilder builder;
    protected DataSource dataSource ;
    
    public Call(EndpointSpec endpoint, Request request) {
        this.endpoint = endpoint;
        this.request = request;
        initBindings();
    }
    
    public Call(API api, String endpointName, Request request) {
        endpoint = api.getSpec(endpointName);
        if (endpoint == null) {
            throw new NotFoundException("Could not locate endpoint specification: " + endpointName);
        }
        this.request = request;
        initBindings();
    }
    
    protected void initBindings() {
        for( Map.Entry<String, String> binding : endpoint.getBindings().entrySet() ) {
            if ( ! request.hasParameter(binding.getKey()) ) {
                request.add(binding.getKey(), binding.getValue());
            }
        }
    }
    
    public String toString() {
    	return "Call{" + endpoint + ", " + request + "}";
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
        if (builder == null) {
            builder = endpoint.getQueryBuilder(request); 
        }
        return builder;
    }
    
    /**
     * Update the query builder corresponding to this call,
     * useful for programmatic endpoints that want to 
     * inject their own processing.
     */
    public void setQueryBuilder(QueryBuilder builder) {
        this.builder = builder;
    }
    
    /**
     * Update the query builder corresponding to this call by applying the given transformation.
     * Useful for programmatic endpoints that want to 
     * inject their own processing.
     */
    public void updateQueryBuilder(Function<QueryBuilder, QueryBuilder> transform) {
        this.builder = transform.apply( getQueryBuilder() );
    }
    
    /**
     * Return the view to be used for this call
     */
    public ViewMap getView() {
        if (request == null) {
            return endpoint.getView();
        } else {
            return endpoint.getView( request.getViewName() );
        }
    }
    
    /**
     * Convert a parameter value string to a well typed
     * value than can be injected into a sparql query.
     * Return null if the mapping can't be found
     */
    public RDFNode prepareParameterValue(String parameter, String value) {
        ViewMap view = getView();
        if (view != null) {
            ViewPath path = view.pathTo(parameter);
            if (path != null) {
                // A legal filter
                request.consume(parameter);
                PropertySpec entry = view.findEntry(path);
                if (entry != null) {
                    String type = entry.getRange();
                    if (type != null) {
                        type = endpoint.getPrefixes().expandPrefix(type);
                    }
                    String valueBase = entry.getValueBase();
                    if (valueBase != null) {
                        valueBase = endpoint.getPrefixes().expandPrefix(valueBase);
                    }
                    if (valueBase != null && ! NameUtils.isURI(value)) {
                        value = NameUtils.ensureLastSlash(valueBase) + value;
                    }
                    return TypeUtil.asTypedValue(value, type);
                }
            }
        }
        return null;
    }
    
    public QueryBuilder finalizeQueryBuilder( QueryBuilder qb ) {
        return endpoint.finalizeQueryBuilder(qb, request);
    }
    
    public QueryBuilder finalizeQueryBuilder( ) {
        return finalizeQueryBuilder( getQueryBuilder() );
    }
    
    /**
     * Return the results for this call, it builds the query and 
     * runs it on the configured data source. Suitable for simple
     * cases where no custom processing of request or query is needed.
     */
    public ResultOrStream getResults() {
        Query query = finalizeQueryBuilder().build();
        checkRequestRecognized();
        try {
            if (query instanceof ListQuery) {
                if (getTemplateName() == null) {
                    templateName = getAPI().getDefaultListTemplate();
                }
                return getDataSource().query((ListQuery)query, this);
            } else {
                if (getTemplateName() == null) {
                    templateName = getAPI().getDefaultItemTemplate();
                }
                return getDataSource().query((ItemQuery)query, this);
            }
        } catch (QueryExceptionHTTP e) {
            if (e.getResponseCode() == 503) {
                throw new WebApiException(e.getResponseCode(), "Query timed out");
            } else {
                log.error("Odd query reponse: " + e.getMessage());
                throw new WebApiException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } catch (ResultSetException e2) {
            throw new WebApiException(Status.INTERNAL_SERVER_ERROR, "Bad response from data server, probably query timeout in mid flight");
        } catch (Exception e3) {
            if ( !(e3 instanceof WebApplicationException) ) {
                log.error("Query problem: " + e3.getMessage());
                throw new WebApiException(Status.INTERNAL_SERVER_ERROR, "Problem with query processing: " + e3);
            } else {
                throw e3;
            }
        }
    }
    
    /**
     * Check that all request parameters have been dealt with, if not treat as a bad request
     */
    public void checkRequestRecognized() {
        request.consume( ExtensionFilter.FORMAT_PARAM );    // Handled by filter before getting to sapi
        List<String> missing = request.getRemainingParameters();
        if ( ! missing.isEmpty() ) {
            throw new WebApiException(Status.BAD_REQUEST, "Did not recognize request parameters " + missing + " as valid for this endpoint, incorrect endpoint?");
        }
    }
    
    /**
     * Return the results for this call using a built (and possible modified) query. 
     */
    public ResultOrStream getResults(Query query) {
        if (query instanceof ListQuery) {
            return getDataSource().query((ListQuery)query, this);
        } else {
            return getDataSource().query((ItemQuery)query, this);
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
    
    /**
     * @return the data source to use for the call
     */
    public DataSource getDataSource() {
        if (dataSource == null) {
            return getAPI().getSource();
        } else {
            return dataSource;
        }
    }
    
    /**
     * Override the default data source.
     * Used to enable the query endpoint to be determined dynamically based on request parameters.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    } 
}
