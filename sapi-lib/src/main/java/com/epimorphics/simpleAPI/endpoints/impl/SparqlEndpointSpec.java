/******************************************************************
 * File:        EndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.endpoints.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.query.impl.DescribeQueryBuilder;
import com.epimorphics.simpleAPI.queryTransforms.AppTransforms;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.sparql.geo.GeoQuery;
import com.epimorphics.sparql.graphpatterns.GraphPatternText;
import com.epimorphics.sparql.query.QueryShape;
import com.epimorphics.sparql.query.Transform;
import com.epimorphics.sparql.query.Transforms;

/**
 * Encapsulates the specification of a single endpoint.
 */
public class SparqlEndpointSpec extends EndpointSpecBase implements EndpointSpec {

	static final Logger log = LoggerFactory.getLogger( SparqlEndpointSpec.class );
	
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
        	if (view == null) baseQuery = createQueryShape();
        	else baseQuery = view.asDescribe();
        }
        return new DescribeQueryBuilder(baseQuery, getPrefixes());
    }

    public QueryShape getBaseQuery() {
    	if (baseQuery == null) baseQuery = createQueryShape();
    	return baseQuery;
    }
    
    public void setBaseQuery(String baseQueryString) {
        getBaseQuery().addEarlyPattern(new GraphPatternText(baseQueryString));
    }

    public void setCompleteQuery(String completeQueryString) {
        getBaseQuery().setTemplate(completeQueryString); 
    }
    
    protected QueryShape createQueryShape() {
    	QueryShape q = new QueryShape();
    	AppTransforms at = (AppTransforms) api.getApp().getComponent("apptransforms");
    	if (at != null) q.getTransforms().addAll(at.transforms);
    	return q;    	
    }

	private void setTransform(QueryShape qs, String name) {
		Transform t = Transforms.get(name);
		if (t == null) throw new RuntimeException("transform '" + name + "' not found.");
		log.debug("using transform " + name);
		qs.getTransforms().add(t);
	}
    
	public void useTransformer(String name) {
		getBaseQuery();
//		setTransform(baseQuery, name);
	}
	
	public void geoQuery(GeoQuery gq) {
		getBaseQuery().setGeoQuery(gq);
	}

}
