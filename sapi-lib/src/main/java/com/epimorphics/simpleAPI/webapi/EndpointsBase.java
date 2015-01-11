/******************************************************************
 * File:        EndpointsBase.java
 * Created by:  Dave Reynolds
 * Created on:  9 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.appbase.templates.VelocityRender;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.DescribeEndpointSpec;
import com.epimorphics.simpleAPI.core.RequestParameters;
import com.epimorphics.simpleAPI.core.ListEndpointSpec;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Shared utilities useful in API implementation.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class EndpointsBase {
    public static final String FULL_MEDIA_TYPE_TURTLE = "text/turtle; charset=UTF-8";
    public static final String MEDIA_TYPE_JSONLD = "application/ld+json";
    public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    public static final String CONTENT_DISPOSITION_FMT = "attachment; filename=\"%s.%s\"";
    
    static final Logger log = LoggerFactory.getLogger( EndpointsBase.class );
    
    protected VelocityRender velocity;
    protected API api;
    
    protected @Context ServletContext context;
    protected @Context UriInfo uriInfo;
    protected @Context HttpServletRequest request;
    
    /**
     * Return the global default API instance.
     */
    public API getAPI() {
        if (api == null) {
            api = AppConfig.getApp().getA(API.class);
        }
        return api;
    }

    /**
     * Return the SPARQL source which this API instance queries
     */
    public SparqlSource getSource() {
        return getAPI().getSource();
    }
    
    /**
     * Return the request URI mapped to the baseURI for this API
     */
    public String getRequestedURI() {
        String path = uriInfo.getPath();
        return getAPI().getBaseURI() + path;
    }
    
    /**
     * Return request summary with just the request base URI
     */
    public RequestParameters getRequest() {
        return new RequestParameters( getRequestedURI() );
    }

    /**
     * Return request summary with the base URI and all parameter included
     */
    public RequestParameters getRequestWithParms() {
        return new RequestParameters( getRequestedURI() ).addParameters(uriInfo);
    }

    /**
     * Describe the item matching the fetched URI using the default mapping specification
     */
    public JSONWritable describeItemJson() {
        return describeItemJson( getAPI().getDefaultDescribe() );
    }

    /**
     * Describe the item matching the fetched URI using the given mapping specification
     */
    public JSONWritable describeItemJson(String specname) {
        return describeItemJson( getAPI().getDescribeSpec(specname) );
    }

    /**
     * Describe the item matching the fetched URI using the given mapping specification
     */
    public JSONWritable describeItemJson(DescribeEndpointSpec spec) {
        RequestParameters rp = getRequest();
        SparqlSource source = getSource();
        Model model = ModelFactory.createModelForGraph( source.describe( spec.getQuery(rp) ) );
        return spec.getWriter( model.getResource( getRequestedURI() ) );
    }
    
    /**
     * Return a list of items based on a supplied query and named mapping endpoint specification
     */
    public JSONWritable listItems(ListEndpointSpec spec, String query, RequestParameters params) {
        log.debug( "List query = " + query);
        ResultSet results = getSource().select(query);
        return spec.getWriter(results, params);
    }
    
    /**
     * Return a list of items based on a named query/mapping endpoint specification, 
     * passing in a pre-built set of request parameters
     */
    public JSONWritable listItems(String specname, RequestParameters params) {
        ListEndpointSpec spec = getAPI().getSelectSpec(specname);
        String query = spec.getQuery(params);
        return listItems(spec, query, params);
    }
    
    /**
     * Return a list of items based on a named query/mapping endpoint specification
     */
    public JSONWritable listItems(String specname) {
        return listItems(specname, getRequest());
    }
    

    /**
     * Return the configured velocity renderer
     */
    public VelocityRender getVelocity() {
        if (velocity == null) {
            velocity =  AppConfig.getApp().getA(VelocityRender.class);
        }
        return velocity;
    }
    
    /**
     * Return streaming render of a velocity template.
     * @param template name fof the template
     * @param args alternating sequence of parameter name/parameter value pairs to pass to the renderer
     */
    public StreamingOutput render(String template, Object...args) {
        return getVelocity().render(template, uriInfo.getPath(), context, uriInfo.getQueryParameters(), args);
    }

    /**
     * Return a setOther redirect to the given target URL
     */
    public Response redirectTo(String path) {
        URI uri;
        try {
            uri = new URI(path);
            return Response.seeOther(uri).build();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }    
}
