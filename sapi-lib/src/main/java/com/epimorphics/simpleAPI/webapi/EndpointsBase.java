/******************************************************************
 * File:        EndpointsBase.java
 * Created by:  Dave Reynolds
 * Created on:  5 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.appbase.templates.VelocityRender;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.endpoints.impl.SparqlEndpointSpec;
import com.epimorphics.simpleAPI.query.DataSource;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.requests.Request;

public class EndpointsBase {
    public static final String FULL_MEDIA_TYPE_TURTLE = "text/turtle; charset=UTF-8";
    public static final String MEDIA_TYPE_JSON_PRIORITY = "application/json;qs=2";
    public static final String FULL_MEDIA_TYPE_CSV = "text/csv; charset=UTF-8";
    public static final String MEDIA_TYPE_JSONLD = "application/ld+json";
    public static final String MEDIA_TYPE_RDFXML = "application/rdf+xml";
    public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    public static final String CONTENT_DISPOSITION_FMT = "attachment; filename=\"%s.%s\"";
    
    static final Logger log = LoggerFactory.getLogger( EndpointsBase.class );
    
    protected VelocityRender velocity;
    protected API api;
    
    protected @Context ServletContext context;
    protected @Context UriInfo uriInfo;
    protected @Context HttpServletRequest httprequest;

    // ---- Generic access methods ---------------------------------
    
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
     * Return the data source which this API instance queries
     */
    public DataSource getSource() {
        return getAPI().getSource();
    }
    
    public VelocityRender getVelocity() {
        return AppConfig.getApp().getA(VelocityRender.class);
    }    

    // ---- Access the request ---------------------------------
    
    /**
     * Return the request URI mapped to the baseURI for this API
     */
    public String getRequestedURI() {
        String path = uriInfo.getPath();
        return getAPI().getBaseURI() + path;
    }
    
    /**
     * Return the full request including parameters
     */
    public Request getRequest() {
        return Request.from(getAPI(), uriInfo, httprequest);
    }
    
    /**
     * Return the full request including query parameters and posted (JSON) body
     */
    public Request getRequest(String body) {
        return Request.from(getAPI(), uriInfo, httprequest, body);
    }

    
    // ---- Standard list endpoint handling ---------------------------------
    
    public Response listResponse(Request request, String endpointName) {
        Call call = new Call(getAPI(), endpointName,request);
        return respondWith( call.getResults() );
    }
    
    /**
     * Handle requests by looking up the path against the set of dynamically
     * configured endpoint patterns.
     */
    public Response defaultResponse() {
        try {
            return respondWith( getAPI().getCall(uriInfo, httprequest).getResults() );
        } catch (NotFoundException e) {
            // default to a describe
            EndpointSpec defaultEndpoint = new SparqlEndpointSpec(getAPI());
            return respondWith( new Call(defaultEndpoint, Request.from(getAPI(), uriInfo, httprequest)).getResults() );
        }
    }
    
    /**
     * Handle requests by looking up the path against the set of dynamically
     * configured endpoint patterns. Pass in a POST (JSON) body as well as the request URL.
     */
    public Response defaultResponse(String body) {
        return respondWith( getAPI().getCall(uriInfo, httprequest, body).getResults() );
    }
    
    // ---- Helpers for returning results ---------------------------------

    /**
     * Return a setOther redirect to the given target URL
     */
    public Response redirectTo(String path) {
        URI uri;
        try {
            uri = new URI(path);
            return Response.seeOther(uri).build();
        } catch (URISyntaxException e) {
            return null;
        }
    }    
        
    /**
     * Return the given entity with maxAge cache control header from the global default in the API config
     */
    public Response respondWith(Object entity) {
        return respondWith(entity, (int)getAPI().getMaxAge());
    }
    
    /**
     * Return the given entity with maxAge cache control header
     * format the given query results according to the endpoint specification
     */
    public Response respondWith(Object entity, int maxAge) {
        CacheControl cc = new CacheControl();
        cc.setMaxAge(maxAge);
        return Response.ok(entity).cacheControl(cc).build();    
    }
    
    // ---- Helpers for Velocity rendering ---------------------------------

    // TODO review context setting
    
    /**
     * Return streaming render of a velocity template.
     * @param template name of the template
     * @param args alternating sequence of parameter name/parameter value pairs to pass to the renderer
     */
    public StreamingOutput render(String template, Object...args) {
        int len = args.length;
        Object[] fullArgs = new Object[len + 2];
        for (int i = 0; i < len; i++) fullArgs[i] = args[i];
        fullArgs[len]   = "baseURI";
        String baseURI = uriInfo.getBaseUri().toString();
        if ( ! baseURI.contains("http://localhost") ) {
            // Use configured base URI unless the request is a localhost (for which we assume this is a test situation)
            baseURI = getAPI().getBaseURI();   
        }
        fullArgs[len+1] = baseURI;
        return getVelocity().render(template, uriInfo.getPath(), context, uriInfo.getQueryParameters(), fullArgs);
    }

    /**
     * Return streaming render of a velocity template.
     * @param template name of the template
     * @param args alternating sequence of parameter name/parameter value pairs to pass to the renderer
     */
    public Response renderResponse(String template, Object...args) {
        return respondWith( render(template, args) );
    }
          
}
