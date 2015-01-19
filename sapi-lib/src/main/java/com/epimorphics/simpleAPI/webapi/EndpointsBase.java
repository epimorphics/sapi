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
import com.epimorphics.appbase.data.WNode;
import com.epimorphics.appbase.data.WSource;
import com.epimorphics.appbase.templates.VelocityRender;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.DescribeEndpointSpec;
import com.epimorphics.simpleAPI.core.RequestParameters;
import com.epimorphics.simpleAPI.core.ListEndpointSpec;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

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
     * Describe the item matching the fetched URI using the default mapping specification
     */
    public JSONWritable describeItemJson() {
        return describeItemJson( getAPI().getDefaultDescribe() );
    }

    /**
     * Describe the already fetched Item as a JSON response
     */
    public JSONWritable describeItemJson(Resource resource) {
        return getAPI().getDefaultDescribe().getWriter(resource);
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
        Model model = describeItem(spec);
        return spec.getWriter( model.getResource( getRequestedURI() ) );
    }

    /**
     * Describe the item matching the fetched URI using a default description
     */
    public Model describeItem() {
        return describeItem( getAPI().getDefaultDescribe() );
    }

    /**
     * Describe the item matching the fetched URI using the given mapping specification
     */
    public Model describeItem(String specname) {
        return describeItem( getAPI().getDescribeSpec(specname) );
    }

    /**
     * Describe the item matching the fetched URI using the given mapping specification
     */
    public Model describeItem(DescribeEndpointSpec spec) {
        RequestParameters rp = getRequest();
        SparqlSource source = getSource();
        Model model = ModelFactory.createModelForGraph( source.describe( spec.getQuery(rp) ) );
        PrefixMapping prefixes = getAPI().getApp().getPrefixes();
        if (prefixes != null) {
            model.setNsPrefixes(prefixes);
        }
        return model;
    }

    /**
     * Describe the item using the supplied query
     */
    public Model describeItemByQuery(String query) {
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

    // ---- Listing items ---------------------------------
    
    /**
     * Return a list of items based on a named query/mapping endpoint specification, 
     * passing in a pre-built set of request parameters
     */
    public JSONWritable listItems(String specname, RequestParameters params) {
        ListEndpointSpec spec = getAPI().getSelectSpec(specname);
        return listItems(spec, params, selectItems(spec, params));
    }
    
    /**
     * Return a JSON stream over the given set of results using the given mapping
     */
    public JSONWritable listItems(String specname, RequestParameters params, ResultSet results) {
        return listItems(getAPI().getSelectSpec(specname), params, results);
    }
    
    /**
     * Return a JSON stream over the given set of results using the given mapping
     */
    public JSONWritable listItems(ListEndpointSpec spec, RequestParameters params, ResultSet results) {
        return spec.getWriter(results, params);
    }
    
    /**
     * Return a list of items based on a named query/mapping endpoint specification
     */
    public JSONWritable listItems(String specname) {
        return listItems(specname, getRequestWithParms());
    }
    
    /**
     * Return the ResultSet from running a configured select query
     */
    public ResultSet selectItems(String specname, RequestParameters params) {
        return selectItems( getAPI().getSelectSpec(specname), params );
    }
    
    /**
     * Return the ResultSet from running a configured select query
     */
    public ResultSet selectItems(ListEndpointSpec spec, RequestParameters params) {
        return selectItems( spec.getQuery(params) );
    }
    
    /**
     * Return the ResultSet from running a supplied select query
     */
    public ResultSet selectItems(String query) {
        log.debug( "List query = " + query);
        return getSource().streamableSelect(query);
    }
    
    // ---- Velocity support ---------------------------------

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
     * Return the SPARQL source, wrapped for using in velocity rendering
     * @return
     */
    public WSource getWSource() {
        return AppConfig.getApp().getA(WSource.class);
    }
    
    public WNode wrap(Resource resource) {
        return new WNode(getWSource(), resource);
    }
    
    /**
     * Return streaming render of a velocity template.
     * @param template name fof the template
     * @param args alternating sequence of parameter name/parameter value pairs to pass to the renderer
     */
    public StreamingOutput render(String template, Object...args) {
        return getVelocity().render(template, uriInfo.getPath(), context, uriInfo.getQueryParameters(), args);
    }

    // ---- Other responses ---------------------------------

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
}
