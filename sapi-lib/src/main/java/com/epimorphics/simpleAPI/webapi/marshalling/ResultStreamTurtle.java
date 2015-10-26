/******************************************************************
 * File:        ResultStreamJSON.java
 * Created by:  Dave Reynolds
 * Created on:  5 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi.marshalling;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.requests.LimitRequestProcessor;
import com.epimorphics.simpleAPI.results.Result;
import com.epimorphics.simpleAPI.results.ResultOrStream;
import com.epimorphics.simpleAPI.results.ResultStream;
import com.epimorphics.vocabs.API;

@Provider
@Produces("text/turtle")
public class ResultStreamTurtle implements MessageBodyWriter<ResultStream> {
    static final Logger log = LoggerFactory.getLogger( ResultStreamTurtle.class );
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        Class<?>[] sigs = type.getInterfaces();
        for (Class<?> sig: sigs) {
            if (sig.equals(ResultStream.class)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getSize(ResultStream t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(ResultStream results, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream)
                    throws IOException, WebApplicationException {
        
        asModel(results).write(entityStream, "Turtle");
    }
    
    public static Model asModel(ResultStream results) {
        Model model = ModelFactory.createDefaultModel();
        
        List<Resource> items = new ArrayList<>();
        for (Result result : results) {
            items.add( result.asResource(model) );
        }
        
        Call call = results.getCall();
        model.setNsPrefixes( call.getEndpoint().getPrefixes() );
        String requestedURI = call.getRequest().getFullRequestedURI();
        Resource root = model.createResource( requestedURI );
        root.addProperty(com.epimorphics.vocabs.API.items,model.createList( items.iterator() ) );
        call.getAPI().addRDFMetadata(root, requestedURI, "ttl");
        condOut(LimitRequestProcessor.LIMIT, results, root, LIMIT_PROP);
        condOut(LimitRequestProcessor.OFFSET, results, root, OFFSET_PROP);
        
        log.info("Returned " + items.size() + " Resources");
        return model;
    }
    
    protected static void condOut(String parameter, ResultOrStream results, Resource meta, Property prop) {
        Long value = results.getRequest().getAsLong(parameter);
        if (value != null) {
            meta.addLiteral(prop, value.longValue());
        }
    }

    protected static Property LIMIT_PROP = ResourceFactory.createProperty(API.NS, "limit");
    protected static Property OFFSET_PROP = ResourceFactory.createProperty(API.NS, "offset");
}
