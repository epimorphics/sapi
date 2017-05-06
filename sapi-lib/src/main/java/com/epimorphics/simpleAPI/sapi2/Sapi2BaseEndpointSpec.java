/******************************************************************
 * File:        Sapi2BaseEndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  27 Apr 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.sapi2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.endpoints.impl.EndpointSpecBase;
import com.epimorphics.simpleAPI.queryTransforms.AppTransforms;
import com.epimorphics.simpleAPI.requests.Request;
import com.epimorphics.simpleAPI.requests.RequestProcessor;
import com.epimorphics.simpleAPI.util.TemplateUtil;
import com.epimorphics.sparql.graphpatterns.GraphPatternText;
import com.epimorphics.sparql.query.QueryShape;
import com.epimorphics.sparql.query.Transform;

public abstract class Sapi2BaseEndpointSpec extends EndpointSpecBase implements EndpointSpec {
    protected String baseQueryString;
    protected String completeQueryString;
    protected Transform transform;
    protected Map<String, String> aliases = new HashMap<>();
    
//    protected QueryShape baseQuery;

    public Sapi2BaseEndpointSpec(API api) {
        super(api);
    }
    public QueryShape getBaseQuery(Request request) {
        return createQueryShape(request);
    }
    
    public void setBaseQuery(String baseQueryString) {
        this.baseQueryString = baseQueryString;
    }

    public void setCompleteQuery(String completeQueryString) {
        this.completeQueryString = completeQueryString;
    }
  
    /**
        Create a QueryShape for this endpoint spec, copying into it the
        transforms from the apptransforms component of this app.
    */
    protected QueryShape createQueryShape(Request request) {
        QueryShape q = new QueryShape();
        if (api != null) {
            AppTransforms at = api.getApp().getComponentAs("apptransforms", AppTransforms.class);
            if (at != null) q.getTransforms().addAll(at.transforms);
        }
        
        if (completeQueryString != null) {
            q.setTemplate( instantiateTemplate(completeQueryString, request) );
        } else if (baseQueryString != null) {
            q.addEarlyPattern(new GraphPatternText( instantiateTemplate(baseQueryString, request) ));
        }
        
        if (api != null) {
            AppTransforms at = (AppTransforms) api.getApp().getComponent("apptransforms");
            if (at != null) q.getTransforms().addAll(at.transforms);
        }
        
        if (transform != null){
            q.getTransforms().add(transform);
        }
        return q;       
    }
    
    protected String instantiateTemplate(String template, Request request) {
        if (TemplateUtil.isTemplate(template)) {
            return TemplateUtil.instatiateTemplate(template, request);
        } else {
            return template;
        }
    }
    
    protected boolean hasExplicitQuery() {
        return baseQueryString != null || completeQueryString != null;
    }

    /**
        Use (make available in this specs transforms) a Transform object
        of the class specified by its name.     
    */
    public void setTransform(String className) {
        try {
            Class<?> c = Class.forName(className);
            Object o = c.newInstance();
            if (o instanceof Transform) {
                transform = (Transform)o;
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
    
    @Override
    public Map<String, String> getAliases() {
        return aliases;
    }
    
    public void addAlias(String from, String to) {
        aliases.put(from, to);
    }
}
