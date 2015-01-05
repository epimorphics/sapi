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

import com.epimorphics.rdfutil.QueryUtil;
import com.hp.hpl.jena.rdf.model.RDFNode;
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
    protected Map<String, RDFNode> bindings = new HashMap<String, RDFNode>();
    protected String filterClause;
    
    public RequestParameters(String uri) {
        this.uri = uri;
    }
    
    public RequestParameters addParameter(String parameter, RDFNode value) {
        bindings.put(parameter, value);
        return this;
    }
    
    public RequestParameters addFilter(String filterClause) {
        this.filterClause = filterClause;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, RDFNode> getBindings() {
        return bindings;
    }

    public String getFilterClause() {
        return filterClause;
    }
    
    public String bindQuery(String query) {
        String q = bindQuery(query, "id", ResourceFactory.createResource(uri) );
        for (Map.Entry<String, RDFNode> entry : bindings.entrySet()) {
            q = bindQuery(q, entry.getKey(), entry.getValue());
        }
        return q;
    }
    
    protected String bindQuery(String query, String var, RDFNode value) {
        return query.replaceAll( "\\?" + var + "\\b", QueryUtil.asSPARQLValue( value ));
    }
    
}

