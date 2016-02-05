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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.ConfigConstants;
import com.epimorphics.simpleAPI.requests.LimitRequestProcessor;
import com.epimorphics.simpleAPI.results.Result;
import com.epimorphics.simpleAPI.results.ResultOrStream;
import com.epimorphics.simpleAPI.results.ResultStream;

@Provider
@Produces("application/json")
public class ResultStreamJSON implements MessageBodyWriter<ResultStream> {
    static final Logger log = LoggerFactory.getLogger( ResultStreamJSON.class );
    
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
        JSFullWriter out = new JSFullWriter(entityStream);
        int count = 0;
        out.startOutput();
        out.startObject();
        writeMetadata(results, out);
        out.key( ConfigConstants.RESULT_ITEMS );
        out.startArray();
        try {
            for (Result result : results) {
                out.arrayElementProcess();
                result.writeJson(out);
                count++;
            }
        } finally {
            results.close();
            out.finishArray();
            out.finishObject();
            out.finishOutput();
        }
        log.info("Returned " + count + " coalesced rows");
    }
    
    public static void writeMetadata(ResultOrStream results, JSFullWriter out) {
        API api = results.getSpec().getAPI();
        api.startMetadata(out);
        api.writeFormats(out, results.getRequest().getFullRequestedURI(), "json");
        condOut("limit", LimitRequestProcessor.APPLIED_LIMIT, results, out);
        condOut("offset", LimitRequestProcessor.OFFSET, results, out);
        api.finishMetadata(out);        
    }
    
    protected static void condOut(String key, String parameter, ResultOrStream results, JSFullWriter out) {
        Long value = results.getRequest().getAsLong(parameter);
        if (value != null) {
            out.pair(key, value.longValue());
        }
    }

}
