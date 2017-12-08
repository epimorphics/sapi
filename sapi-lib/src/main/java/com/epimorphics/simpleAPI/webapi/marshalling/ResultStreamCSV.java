/******************************************************************
 * File:        ResultStreamJSON.java
 * Created by:  Dave Reynolds
 * Created on:  5 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi.marshalling;

import static com.epimorphics.simpleAPI.webapi.EndpointsBase.CONTENT_DISPOSITION_FMT;
import static com.epimorphics.simpleAPI.webapi.EndpointsBase.CONTENT_DISPOSITION_HEADER;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.query.DataSource;
import com.epimorphics.simpleAPI.query.impl.SparqlDataSource;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.results.Result;
import com.epimorphics.simpleAPI.results.ResultOrStream;
import com.epimorphics.simpleAPI.results.ResultStream;
import com.epimorphics.simpleAPI.results.TreeResult;
import com.epimorphics.simpleAPI.util.LastModified;
import com.epimorphics.simpleAPI.writers.CSVWriter;

@Provider
@Produces("text/csv")
public class ResultStreamCSV implements MessageBodyWriter<ResultStream> {
    static final Logger log = LoggerFactory.getLogger( ResultStreamCSV.class );
    
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
    
    public static void injectFilename(ResultOrStream result, MultivaluedMap<String, Object> httpHeaders) {
        Call call = result.getCall();
        API api = call.getAPI();
        if ( api.isGenerateCSVfilenames() ) {
            String filename = call.getRequest().asFilename();
            LastModified lm = api.getTimestampService();
            if (lm != null) {
                DataSource source = api.getSource();
                if (source instanceof SparqlDataSource) {
                    Long ts = lm.getTimestamp( (SparqlDataSource)source );
                    if (ts != null) {
                        filename += "-lastmod-" + new SimpleDateFormat("yyyyMMdd-HHmm").format( new Date(ts) );
                    }
                }
            }
            filename += ".csv";
            httpHeaders.add(CONTENT_DISPOSITION_HEADER, String.format(CONTENT_DISPOSITION_FMT, filename));    
        }
    }

    @Override
    public void writeTo(ResultStream results, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream)
                    throws IOException, WebApplicationException {
        injectFilename(results, httpHeaders);
        CSVWriter writer = new CSVWriter(entityStream);
        if( results.getCall().getEndpoint().isSuppressID() ) {
            writer.setIncludeID(false);
        }
        int count = 0;
        try {
            for (Result result : results) {
                if (result instanceof TreeResult) {
                    writer.write( (TreeResult)result );
                } else {
                    throw new WebApiException(Status.UNSUPPORTED_MEDIA_TYPE, "Serializing RDF description as CSV not supported");
                }
                count++;
            }
        } finally {
            results.close();
            writer.close();
        }
        log.info("Returned " + count + " coalesced rows");
    }

}
