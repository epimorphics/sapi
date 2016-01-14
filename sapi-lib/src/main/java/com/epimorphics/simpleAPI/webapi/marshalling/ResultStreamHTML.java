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
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.appbase.templates.VelocityRender;
import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.requests.Request;
import com.epimorphics.simpleAPI.results.Result;
import com.epimorphics.simpleAPI.results.ResultStream;
import com.epimorphics.simpleAPI.results.wappers.WResult;

@Provider
@Produces("text/html")
public class ResultStreamHTML implements MessageBodyWriter<ResultStream> {
    static final Logger log = LoggerFactory.getLogger( ResultStreamHTML.class );
    
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
        List<WResult> wresults = new ArrayList<>();
        for (Result result : results) {
            wresults.add( new WResult(result) );
        }
        
        Call call = results.getCall();
        Request request = call.getRequest();
        API api = call.getAPI();
        VelocityRender velocity = api.getApp().getA(VelocityRender.class);
        Map<String, Object> bindings = request.getRenderBindings();
        // Add the result in, this side-effects the request render bindings but that's OK, it is use-once
        bindings.put("results", wresults);
        try {
            velocity.renderTo(entityStream, call.getTemplateName(), AppConfig.getAppConfig().getContext(), bindings);
        } catch (org.apache.velocity.exception.ResourceNotFoundException e) {
            throw new WebApiException(Status.INTERNAL_SERVER_ERROR, "HTML rendering not available");
        }
    }

}
