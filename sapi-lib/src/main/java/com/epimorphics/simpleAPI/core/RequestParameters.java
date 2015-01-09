/******************************************************************
 * File:        RequestParameters.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import com.epimorphics.rdfutil.QueryUtil;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * <p>Encapsulates the request parameters. This can be used to bind a query template
 * to a concrete query.</p>
 * <p>The request parameters comprise:
 * <ul>
 *   <li>the target URI (after mapping from request URI to the API base URI)</li>
 *   <li>any query/path parameters mapping to RDF Nodes</li>
 *   <li>any externally injected query filter clauses</li>
 * </ul>
 * </p>
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class RequestParameters {

    protected String uri;
    protected Map<String, Object> bindings = new HashMap<String, Object>();
    protected String filterClause;
    
    public RequestParameters(String uri) {
        this.uri = uri;
    }
    
    public RequestParameters addParameter(String parameter, Object value) {
        bindings.put(parameter, value);
        return this;
    }
    
    public RequestParameters addParameters(UriInfo info) {
        addParameters( info.getPathParameters() );
        addParameters( info.getQueryParameters() );
        return this;
    }
    
    protected RequestParameters addParameters(MultivaluedMap<String, String> params) {
        for (String key : params.keySet()) {
            addParameter(key, params.getFirst(key));
        }
        return this;
    }
    
    public RequestParameters addFilter(String filterClause) {
        this.filterClause = filterClause;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public String getFilterClause() {
        return filterClause;
    }
    
    public String bindQuery(String query) {
        return bindQueryID( bindQueryParams(query) );
    }

    /**
     * Bind the ?id variable in the query to the requested URI
     */
    public String bindQueryID(String query) {
        return bindQuery(query, "id", ResourceFactory.createResource(uri) );
    }

    /**
     * Bind all variables in the query that match the request bindings
     */
    public String bindQueryParams(String query) {
        String q = query;
        for (Map.Entry<String, Object> entry : bindings.entrySet()) {
            q = bindQuery(q, entry.getKey(), entry.getValue());
        }
        return q;
    }
    
    protected String bindQuery(String query, String var, Object value) {
        return query.replaceAll( "\\?" + var + "\\b", QueryUtil.asSPARQLValue( value ));
    }
    
}

