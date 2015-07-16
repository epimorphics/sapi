/******************************************************************
 * File:        EPBuilder.java
 * Created by:  Dave Reynolds
 * Created on:  9 Feb 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi;

import javax.servlet.ServletContext;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.appbase.data.WSource;
import com.epimorphics.appbase.templates.VelocityRender;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.RequestParameters;

/**
 * Abstract base class for API response builders.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public abstract class EPResponseBuilder {
    public enum Format { json, csv, rdf, html };
    
    protected Format format = Format.json;
    protected String htmlTemplate;
    protected Object[] templateArgs;
    protected API api;
    protected Integer maxAge = null;
    protected UriInfo uriInfo;
    protected ServletContext context;
    protected String requestedURI;
    protected String baseRequestedURI;
    protected boolean csvIncludeID = true; 
    
    public EPResponseBuilder(ServletContext context, UriInfo uriInfo) {
        this.context = context;
        this.uriInfo = uriInfo;
        
        String rawRequest = uriInfo.getRequestUri().toString();
        String path = uriInfo.getPath();
        baseRequestedURI = getAPI().getBaseURI() + path;
        requestedURI = baseRequestedURI;
        if (rawRequest.contains("?")) {
            String query = rawRequest.substring( rawRequest.indexOf('?') );
            requestedURI += query;
        }

    }
    
    /**
     * Set the response to format as json
     */
    public EPResponseBuilder asJson() {
        format = Format.json;
        return this;
    }

    /**
     * Set the response to format as a CSV
     */
    public EPResponseBuilder asCSV() {
        format = Format.csv;
        return this;
    }

    /**
     * Set the response to format as a CSV
     * @param includeID set to false to suppress inclusion of @id information on the returned objects
     */
    public EPResponseBuilder asCSV(boolean includeID) {
        format = Format.csv;
        csvIncludeID = includeID;
        return this;
    }

    /**
     * Set the response to format as RDF (the entity will be Model,
     * up to the registered serializes for Model to map that to 
     * the specific requested Mime type)
     */
    public EPResponseBuilder asRDF() {
        format = Format.rdf;
        return this;
    }

    /**
     * Set the response to format as HTML using the given template.
     * In the velocity context a (wrapped) described entity will be bound to "resource" 
     * a list of search results will be bound to "results"
     * @param template the name of the velocity templates
     * @param args alternating pairs of velocity context name and value to bind to
     */
    public EPResponseBuilder asHtml(String template,  Object... args) {
        format = Format.html;
        this.htmlTemplate = template;
        this.templateArgs = args;
        return this;
    }

    /**
     * Set the arguments to be added to any velocity rendering of this response.
     * Replaces any arguments already set. Only meaningful if asHtml has been or will be invoked.
     * @param args alternating pairs of velocity context name and value to bind to
     */
    public EPResponseBuilder setTemplateArgs( Object... args) {
        this.templateArgs = args;
        return this;
    }
    
    /** Return the entity body for the response */
    public abstract Object getEntity();
    
    /** Return the variable name to use for the entity when performing velocity rendering */
    public abstract String getEntityVelocityName();
    
    public Response respond() {
        Object entity = getEntity();
        if (format == Format.html) {
            int len = templateArgs.length;
            Object[] fullArgs = new Object[len + 4];
            int x = 0;
            
            fullArgs[x++] = "baseURI";
            String baseURI = uriInfo.getBaseUri().toString();
            if ( ! baseURI.contains("http://localhost") ) {
                // Use configured base URI unless the request is a localhost (for which we assume this is a test situation)
                baseURI = getAPI().getBaseURI();   
            }
            fullArgs[x++] = baseURI;
            
            fullArgs[x++] = getEntityVelocityName();
            fullArgs[x++] = entity;
            
            for (int i = 0; i < len; i++) fullArgs[x++] = templateArgs[i];
            entity = getVelocity().render(htmlTemplate, uriInfo.getPath(), context, uriInfo.getQueryParameters(), fullArgs);
        }            
        CacheControl cc = new CacheControl();
        cc.setMaxAge( getMaxAge() );
        return Response.ok( entity )
                .cacheControl(cc)
                .build();
    }
    
    /**
     * Return the request URI mapped to the baseURI for this API
     * Includes query parameters
     */
    public String getRequestedURI() {
        return requestedURI;
    }
    
    /**
     * Return the request URI mapped to the baseURI for this API
     * Excludes query parameters
     */
    public String getBaseRequestedURI() {
        return baseRequestedURI;
    }
    
    /**
     * Return request summary with just the request base URI
     */
    public RequestParameters getRequest() {
        return new RequestParameters( getBaseRequestedURI() );
    }

    /**
     * Return request summary with the base URI and all parameter included
     */
    public RequestParameters getRequestWithParms() {
        return new RequestParameters( getBaseRequestedURI() ).addParameters(uriInfo);
    }
    
    /**
     * Set the cache max-age in seconds
     */
    public EPResponseBuilder setMaxAge(int maxAge) {
        this.maxAge = maxAge;
        return this;
    }
    
    public int getMaxAge() {
        if (maxAge != null) {
            return maxAge;
        } else {
            return (int) getAPI().getMaxAge();
        }
    }
    
    /**
     * Return the API, uses the global default if nothing has been set
     */
    public API getAPI() {
        if (api == null) {
            api = AppConfig.getApp().getA(API.class);
        }
        return api;
    }
    
    public EPResponseBuilder setAPI(API api) {
        this.api = api;
        return this;
    }
    
    protected SparqlSource getSource() {
        return getAPI().getSource();
    }
    
    protected VelocityRender getVelocity() {
        return AppConfig.getApp().getA(VelocityRender.class);
    }    
    
    /**
     * Return the SPARQL source, wrapped for use in velocity rendering
     */
    protected WSource getWSource() {
        return AppConfig.getApp().getA(WSource.class);
    }
    
    
}
