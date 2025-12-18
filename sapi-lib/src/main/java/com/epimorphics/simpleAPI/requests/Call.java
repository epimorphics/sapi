/******************************************************************
 * File:        Call.java
 * Created by:  Dave Reynolds
 * Created on:  6 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import com.epimorphics.appbase.webapi.ExtensionFilter;
import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.rdfutil.TypeUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.*;
import com.epimorphics.simpleAPI.results.ResultOrStream;
import com.epimorphics.simpleAPI.views.ViewEntry;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ViewPath;
import com.epimorphics.util.NameUtils;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import java.util.List;
import java.util.function.Function;

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
    }
    
    public Call(API api, String endpointName, Request request) {
        endpoint = api.getSpec(endpointName);
        if (endpoint == null) {
            throw new NotFoundException("Could not locate endpoint specification: " + endpointName);
        }
        this.request = request;
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
                ViewEntry entry = view.findEntry(path);
                if (entry != null) {
                    String type = entry.getTypeURI();
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
    public ResultOrStream getResults() throws WebApiException {
        Query query = finalizeQueryBuilder().build();
        log.info("Query [" +  MDC.get("transaction_id") + "] " + query);
        checkRequestRecognized();
        try {
            return getResults(query);
        } catch (QueryExceptionHTTP e) {
            // Retry before reporting error?
            if (e.getStatusCode() >= 500) {
                log.warn("Sparql query execution failed, retrying");
                try {
                    Thread.sleep(3000);  // TODO make configurable
                } catch (InterruptedException ex) {
                }
                try {
                    return getResults(query);
                } catch (Exception er) {
                    returnError(er);
                }
            }
            returnError(e);
        }  catch (Exception e2) {
            returnError(e2);
        }
        return null;
    }

    public void returnError(Exception e) throws WebApiException, WebApplicationException {
        if (e instanceof QueryExceptionHTTP) {
            // Maybe a bad query, a timeout, or dead fuseki
            int status = ((QueryExceptionHTTP)e).getStatusCode();
            if (status == 503) {
                // Fuseki returns 503 when queries time out
                returnError(status, "Query timed out");
            }
            returnError(status, e.getMessage());
        } else if (e instanceof ResultSetException) {
            returnError(504, "Bad response from data server, probably query timeout in mid flight");
        } else if (e instanceof WebApplicationException) {
            // 404 etc handled directly in jax and doesn't need separate logging
            throw (WebApplicationException) e;
        } else {
            returnError(500, e.getMessage());
        }
    }

    public void returnError(int status, String message) throws WebApiException {
        if (status >= 500) {
            log.error("Query failed [{}]: {}", MDC.get("transaction_id"), message);
        } else {
            log.warn("Query failed [{}]: {}", MDC.get("transaction_id"), message);
        }
        throw new WebApiException(status, message);
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
