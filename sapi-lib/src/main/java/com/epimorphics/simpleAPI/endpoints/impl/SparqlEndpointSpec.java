/******************************************************************
 * File:        EndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.endpoints.impl;

import org.apache.jena.shared.PrefixMapping;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.query.impl.SparqlQueryBuilder;
import com.epimorphics.util.PrefixUtils;

/**
 * Encapsulates the specification of a single endpoint.
 */
public class SparqlEndpointSpec extends EndpointSpecBase implements EndpointSpec {
    protected String baseQuery;
    protected PrefixMapping localPrefixes;
    protected PrefixMapping prefixes;
    protected QueryBuilder queryBuilder;
    
    public SparqlEndpointSpec(API api) {
        super(api);
    }
    
    /**
     * Return the prefix bindings defined for this Endpoint (including the App-wide prefixes)
     */
    public PrefixMapping getPrefixes() {
        if (prefixes == null) {
            if (api != null && api.getApp() != null) {
                prefixes = api.getApp().getPrefixes();
            }
            if (prefixes == null) {
                prefixes = localPrefixes;
            } else if (localPrefixes != null) {
                prefixes = PrefixUtils.merge(prefixes, localPrefixes);
            }
        }
        return prefixes;
    }

    /**
     * Add prefix declaration specific to this endpoint
     */
    public void addLocalPrefix(String prefix, String uri) {
        if (localPrefixes == null) {
            localPrefixes = PrefixMapping.Factory.create();
        }
        localPrefixes.setNsPrefix(prefix, uri);
    }


    @Override
    public QueryBuilder getQueryBuilder() {
        if (queryBuilder == null) {
            String q = baseQuery;
            if (view != null) {
                q = q + "\n" + view.asQuery();
            }
            queryBuilder = SparqlQueryBuilder.fromBaseQuery(q, getPrefixes());
        }
        return queryBuilder;
    }

    public void setBaseQuery(String query) {
        this.baseQuery = query;
    }

}
