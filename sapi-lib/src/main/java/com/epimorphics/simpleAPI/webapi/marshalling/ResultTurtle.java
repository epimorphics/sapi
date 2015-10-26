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

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.results.Result;

@Provider
@Produces("text/turtle")
public class ResultTurtle implements MessageBodyWriter<Result> {
    static final Logger log = LoggerFactory.getLogger( ResultTurtle.class );
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        Class<?>[] sigs = type.getInterfaces();
        for (Class<?> sig: sigs) {
            if (sig.equals(Result.class)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getSize(Result t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Result result, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream)
                    throws IOException, WebApplicationException {
        asModel(result).write(entityStream, "Turtle");
    }

    public static Model asModel(Result result) {
        Resource root = result.asResource();
        Model model = root.getModel();
        Call call = result.getCall();
        model.setNsPrefixes( call.getEndpoint().getPrefixes() );
        Resource meta = model.createResource(); 
        call.getAPI().addRDFMetadata(meta, call.getRequest().getFullRequestedURI(), "ttl");
        meta.addProperty(FOAF.primaryTopic, root);
        return model;
    }
}
