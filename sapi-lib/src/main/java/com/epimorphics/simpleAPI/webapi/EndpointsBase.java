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
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.appbase.templates.VelocityRender;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.ListEndpointSpec;
import com.epimorphics.simpleAPI.core.RequestParameters;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Shared utilities useful in API implementation.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class EndpointsBase {
    public static final String FULL_MEDIA_TYPE_TURTLE = "text/turtle; charset=UTF-8";
    public static final String MEDIA_TYPE_JSONLD = "application/ld+json";
    public static final String MEDIA_TYPE_RDFXML = "application/rdf+xml";
    public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    public static final String CONTENT_DISPOSITION_FMT = "attachment; filename=\"%s.%s\"";
    
    static final Logger log = LoggerFactory.getLogger( EndpointsBase.class );
    
    protected VelocityRender velocity;
    protected API api;
    
    protected @Context ServletContext context;
    protected @Context UriInfo uriInfo;
    protected @Context HttpServletRequest request;

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
     * Return the SPARQL source which this API instance queries
     */
    public SparqlSource getSource() {
        return getAPI().getSource();
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

    // ---- Describing individual items ---------------------------------

    /**
     * Describe a resource specified by its URI
     */
    public DescribeResponseBuilder startDescribeByURI(String uri) {
        return startDescribe().describeByURI(uri);
    }
    
    /**
     * Describe the requested URI using the named describe specification
     */
    public DescribeResponseBuilder startDescribe(String specname) {
        return startDescribe().describe(specname);
    }
    
    /**
     * Describe the requested URI using the given describe specification
     */
    public DescribeResponseBuilder startDescribe() {
        return new DescribeResponseBuilder(context, uriInfo);
    }    

    // ---- Return lists of items ---------------------------------
    
    /**
     * Initiate a list response, use builder methods to specify the query
     */
    public ListResponseBuilder startList() {
        return new ListResponseBuilder(context, uriInfo);
    }

    /**
     * list items based on a named query/mapping endpoint specification
     */
    public ListResponseBuilder startList(String specname) {
        return startList().list(specname);
    }

    /**
     * list items based on a named query/mapping endpoint specification, 
     * passing in a pre-built set of request parameters
     */
    public ListResponseBuilder startList(String specname, RequestParameters params) {
        return startList().list(specname, params);
    }
    
    /**
     * format the given query results according to the endpoint specification
     */
    public ListResponseBuilder startList(String specname, RequestParameters params, ResultSet results) {
        return startList().list(specname, params, results);
    }
    
    /**
     * format the given query results according to the endpoint specification
     */
    public ListResponseBuilder startList(ListEndpointSpec spec, RequestParameters params, ResultSet results) {
        return startList().list(spec, params, results);
    }
        
    // ---- Other responses ---------------------------------
    
    /**
     * Describe a resource returned by an inline describe query
     */
    public Model describeByQuery(String query) {
        SparqlSource source = getSource();
        return ModelFactory.createModelForGraph( source.describe( query ) );
    }

    /**
     * Vanilla describe on the given URI
     */
    public Resource describeItemByURI(String uri) {
        SparqlSource source = getSource();
        Model model = ModelFactory.createModelForGraph( source.describe( "DESCRIBE <" + uri + ">" ) );
        return model.getResource(uri);
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
            return null;
        }
    }    
    

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
    
    protected VelocityRender getVelocity() {
        return AppConfig.getApp().getA(VelocityRender.class);
    }    
        
    
}
