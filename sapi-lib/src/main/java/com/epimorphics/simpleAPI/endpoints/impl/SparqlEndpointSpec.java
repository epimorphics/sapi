/******************************************************************
 * File:        EndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.endpoints.impl;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.query.impl.SparqlQueryBuilder;

/**
 * Encapsulates the specification of a single endpoint.
 */
public class SparqlEndpointSpec extends EndpointSpecBase implements EndpointSpec {
    protected String baseQuery;
    protected QueryBuilder queryBuilder;
    
    public SparqlEndpointSpec(API api) {
        super(api);
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
