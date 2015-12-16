/******************************************************************
 * File:        EndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  27 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.endpoints.impl;

import javax.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.query.impl.DescribeQueryBuilder;
import com.epimorphics.simpleAPI.queryTransforms.AppTransforms;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.sparql.graphpatterns.GraphPatternText;
import com.epimorphics.sparql.query.QueryShape;
import com.epimorphics.sparql.query.Transform;

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
    
    /**
		Create a QueryShape for this endpoint spec, copying into it the
		transforms from the apptransforms component of this app.
    */
    protected QueryShape createQueryShape() {
    	QueryShape q = new QueryShape();
    	AppTransforms at = (AppTransforms) api.getApp().getComponent("apptransforms");
    	if (at != null) q.getTransforms().addAll(at.transforms);
    	return q;    	
    }

    /**
		Use (make available in this specs transforms) a Transform object
		of the class specified by its name.     
    */
	public void useTransformByClassName(String className) {
		QueryShape qs = getBaseQuery();
		try {
			Class<?> c = Class.forName(className);
			Object o = c.newInstance();
			if (o instanceof Transform) {
				qs.getTransforms().add((Transform) o);
			} else {
				throw new RuntimeException(className + " is not a transform");
			}
		} catch (ClassNotFoundException e) {
			throw new NotFoundException("class " + className);
		} catch (InstantiationException e) {
			throw new RuntimeException("could not instantiate class " + className);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("could not access class " + className);
		}
	}

}
