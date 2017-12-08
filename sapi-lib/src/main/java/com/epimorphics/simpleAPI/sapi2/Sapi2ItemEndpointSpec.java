/******************************************************************
 * File:        EndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.sapi2;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.query.impl.DescribeQueryBuilder;
import com.epimorphics.simpleAPI.requests.Request;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.sparql.query.QueryShape;

/**
 * Encapsulates the specification of a single endpoint.
 */
public class Sapi2ItemEndpointSpec extends Sapi2BaseEndpointSpec implements EndpointSpec {
    
    public Sapi2ItemEndpointSpec(API api) {
        super(api);
    }

    @Override public QueryBuilder getQueryBuilder(String viewname, Request request) {
        ViewMap view = getView(viewname);
        if (view == null) {
            view = getView(DEFAULT_VIEWNAME);
        }
        
        QueryShape q = (hasExplicitQuery() || view == null) ? getBaseQuery(request) : view.asDescribe();

        return new DescribeQueryBuilder(q, getPrefixes());
    }

}
