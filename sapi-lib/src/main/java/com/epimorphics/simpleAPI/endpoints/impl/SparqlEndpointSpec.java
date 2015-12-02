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
import com.epimorphics.simpleAPI.query.impl.DescribeQueryBuilder;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.sparql.graphpatterns.GraphPatternText;
import com.epimorphics.sparql.query.QueryShape;
import com.epimorphics.sparql.query.Transform;
import com.epimorphics.sparql.query.Transforms;

/**
 * Encapsulates the specification of a single endpoint.
 */
public class SparqlEndpointSpec extends EndpointSpecBase implements EndpointSpec {
	
    protected QueryShape baseQuery;
    
    public SparqlEndpointSpec(API api) {
        super(api);
    }

    @Override public QueryBuilder getQueryBuilder() {
        return getQueryBuilder(DEFAULT_VIEWNAME);
    }

    @Override public QueryBuilder getQueryBuilder(String viewname) {
        ViewMap view = getView(viewname);
        if (baseQuery == null) {
        	if (view == null) baseQuery = new QueryShape();
        	else baseQuery = view.asDescribe();
        }
        return new DescribeQueryBuilder(baseQuery, getPrefixes());
    }

    public void setBaseQuery(String baseQueryString) {
    	if (baseQuery == null) baseQuery = new QueryShape();
        baseQuery.addEarlyPattern(new GraphPatternText(baseQueryString));
    }

    public void setCompleteQuery(String completeQueryString) {
    	if (baseQuery == null) baseQuery = new QueryShape();
        baseQuery.setTemplate(completeQueryString); 
    }
    
    protected QueryShape createQueryShape() {
    	QueryShape q = new QueryShape();
    	return q;    	
    }

	public void useTransformer(String name) {
		if (baseQuery == null) baseQuery = new QueryShape();
		Transform t = Transforms.get(name);		
		Transforms ts = baseQuery.getTransforms();
		ts.add(name, t);
	}

}
