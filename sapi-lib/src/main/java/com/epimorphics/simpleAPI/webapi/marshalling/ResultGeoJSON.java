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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.simpleAPI.results.Result;
import com.epimorphics.simpleAPI.results.TreeResult;
import com.epimorphics.simpleAPI.writers.GeojsonWriter;

@Provider
@Produces("application/geo+json")
public class ResultGeoJSON implements MessageBodyWriter<Result> {
    static final Logger log = LoggerFactory.getLogger( ResultGeoJSON.class );
    
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
        JSFullWriter out = new JSFullWriter(entityStream);
        GeojsonWriter writer = new GeojsonWriter(out);
        if (! (result instanceof TreeResult) ) {
            throw new WebApiException(Status.BAD_REQUEST, "Cannot render as GeoJson");
        }
        out.startOutput();
        try {
            writer.write( (TreeResult) result);
        } finally {
            out.finishOutput();
        }
    }    

}
