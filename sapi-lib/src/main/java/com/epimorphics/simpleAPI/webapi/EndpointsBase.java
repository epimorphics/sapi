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

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.appbase.templates.VelocityRender;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.writers.ResourceJsonWriter;
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
    
    protected VelocityRender velocity;
    protected API api;
    
    protected @Context ServletContext context;
    protected @Context UriInfo uriInfo;
    protected @Context HttpServletRequest request;
    
    public API getAPI() {
        if (api == null) {
            api = AppConfig.getApp().getA(API.class);
        }
        return api;
    }
    
    public SparqlSource getSource() {
        return getAPI().getSource();
    }
    
    public VelocityRender getVelocity() {
        if (velocity == null) {
            velocity =  AppConfig.getApp().getA(VelocityRender.class);
        }
        return velocity;
    }
    
    public String getRequestedURI() {
        String path = uriInfo.getPath();
        return getAPI().getBaseURI() + path;
    }

    public JSONWritable describeItemJson(String query, String itemname) {
        SparqlSource source = getSource();
        Model model = ModelFactory.createModelForGraph( source.describe(query) );
        return new ResourceJsonWriter(getAPI(), itemname, model.getResource( getRequestedURI() ));        
    }

    public JSONWritable describeItemJson() {
        return describeItemJson("DESCRIBE <" + getRequestedURI() + ">", "item");
    }
    
    /*
    public JSONWritable listItems(SelectMapping mapping) {
        return new MappedResultSetJsonWriter( getAPI(), mapping, getSource().select( mapping.asQuery() ) );        
    }
    
    public JSONWritable listItems(SelectMapping mapping, String qualifier) {
        return new MappedResultSetJsonWriter( getAPI(), mapping, getSource().select( mapping.asQuery() + qualifier ) );        
    }
    
    public JSONWritable listFilteredItems(SelectMapping mapping, String filter) {
        return new MappedResultSetJsonWriter( getAPI(), mapping, getSource().select( mapping.asQuery(filter) ) );        
    }
    
    public JSONWritable listFilteredItems(SelectMapping mapping, String filter, String qualifier) {
        return new MappedResultSetJsonWriter( getAPI(), mapping, getSource().select( mapping.asQuery(filter) + qualifier ) );        
    }
    */
    
    public StreamingOutput render(String template, Object...args) {
        return getVelocity().render(template, uriInfo.getPath(), context, uriInfo.getQueryParameters(), args);
    }

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
