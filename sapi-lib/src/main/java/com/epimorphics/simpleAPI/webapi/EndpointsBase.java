/******************************************************************
 * File:        EndpointsBase.java
 * Created by:  Dave Reynolds
 * Created on:  5 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.appbase.templates.VelocityRender;
import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.endpoints.impl.SparqlEndpointSpec;
import com.epimorphics.simpleAPI.query.DataSource;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.query.impl.SparqlDataSource;
import com.epimorphics.simpleAPI.query.impl.SparqlQueryBuilder;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.requests.Request;
import com.epimorphics.simpleAPI.results.ResultOrStream;
import com.epimorphics.simpleAPI.util.LastModified;

public class EndpointsBase {
    public static final String TURTLE = "text/turtle; charset=UTF-8";
    public static final String CSV = "text/csv; charset=UTF-8";
    public static final String JSONLD = "application/ld+json";
    public static final String RDFXML = "application/rdf+xml";
    
    public static MediaType TURTLE_TYPE;
    public static MediaType CSV_TYPE;
    public static MediaType JSONLD_TYPE;
    public static MediaType RDFXML_TYPE;
    public static  List<Variant> nonHtmlVariants;
    public static  List<Variant> htmlVariants;
    
    public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    public static final String CONTENT_DISPOSITION_FMT = "attachment; filename=\"%s\"";
    
    static {
        Map<String,String> nonPreferred = new HashMap<>();
        nonPreferred.put("qs", "0.5");
        
        Map<String,String> nonPreferredUTF8 = new HashMap<>( nonPreferred );
        nonPreferredUTF8.put("charset", "UTF-8");
        
        TURTLE_TYPE = new MediaType("text", "turtle", nonPreferredUTF8);
        CSV_TYPE    = new MediaType("text", "csv", nonPreferredUTF8);
        JSONLD_TYPE = new MediaType("application", "ld+json", nonPreferred);
        RDFXML_TYPE = new MediaType("application", "rdf+xml", nonPreferred);
        
        nonHtmlVariants = Variant.mediaTypes(
                MediaType.APPLICATION_JSON_TYPE,
                TURTLE_TYPE,
                RDFXML_TYPE,
                JSONLD_TYPE,
                CSV_TYPE).build();
        htmlVariants = Variant.mediaTypes( MediaType.TEXT_HTML_TYPE ).build();
    }
    
    static final Logger log = LoggerFactory.getLogger( EndpointsBase.class );
    
    protected VelocityRender velocity;
    protected API api;
    protected Request request;
    
    protected @Context ServletContext context;
    protected @Context UriInfo uriInfo;
    protected @Context HttpServletRequest httprequest;
    protected @Context javax.ws.rs.core.Request containerRequest;

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
     * Return the data source which this API instance queries
     */
    public DataSource getSource() {
        return getAPI().getSource();
    }
    
    public VelocityRender getVelocity() {
        return AppConfig.getApp().getA(VelocityRender.class);
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
     * Return the full request including parameters
     */
    public Request getRequest() {
        if (request == null) {
            request = Request.from(getAPI(), uriInfo, httprequest); 
        }
        return request;
    }
    
    /**
     * Return the full request including query parameters and posted (JSON) body
     */
    public Request getRequest(String body) {
        if (request == null) {
            request = Request.from(getAPI(), uriInfo, httprequest, body);
        }
        return request;
    }
    
    /**
     * Return a call package containing the dynamically located endpoint and the request
     */
    public Call getCall() {
        checkLastModified();
        try {
            return getAPI().getCall(uriInfo, getRequest() );
        } catch (NotFoundException e) {
            EndpointSpec defaultEndpoint = new SparqlEndpointSpec(getAPI());
            return new Call(defaultEndpoint, getRequest());
        }
    }
    
    /**
     * Return a call package containing the specified endpoint and the request
     */
    public Call getCall(String endpoint) {
        checkLastModified();
        return getAPI().getCall(endpoint, getRequest());
    }
    
    /**
     * Handle if-modified-since processing of the request has that header and
     * if the API config supports modification timestamping. Currently only supports
     * a global timestamp, not per-endpoint timestamps. Automatically callsed from {@link #getCall()} 
     * so only need be directly called in special cases.
     */
    public void checkLastModified() {
        Date lastModified = getLastModifedIfAvailable();
        if (lastModified != null) {
            try {
                ResponseBuilder builder = containerRequest.evaluatePreconditions(lastModified);
                if (builder != null) {
                    throw new NotModifiedException(lastModified);
                }
            } catch (Exception e) {
                // Ignore and let normal processing continue
            }
        }
    }

    private Date getLastModifedIfAvailable() {
        LastModified lm = getAPI().getTimestampService();
        if (lm != null) {
            DataSource source = getAPI().getSource();
            if (source instanceof SparqlDataSource) {
                Long ts = lm.getTimestamp( (SparqlDataSource)source );
                if (ts != null) {
                    return new Date(ts);
                }
            }
        }
        return null;
    }
    
    // ---- Standard list endpoint handling ---------------------------------
    
    public Response listResponse(Request request, String endpointName) {
        Call call = new Call(getAPI(), endpointName,request);
        return respondWith( call.getResults() );
    }
    
    /**
     * Handle requests by looking up the path against the set of dynamically
     * configured endpoint patterns.
     */
    public Response defaultResponse() {
        return respondWith( getCall().getResults() );
    }
    
    /**
     * Handle requests by looking up the path against the set of dynamically
     * configured endpoint patterns. Pass in a POST (JSON) body as well as the request URL.
     */
    public Response defaultResponse(String body) {
        return respondWith( getAPI().getCall(uriInfo, httprequest, body).getResults() );
    }
    
    // ---- Helpers for query manipulation ---------------------------------
    
    /**
     * Add a sort on the given parameter
     */
    public void sort(Call call, String param, boolean down) {
        SparqlQueryBuilder builder = (SparqlQueryBuilder)call.getQueryBuilder();
        call.setQueryBuilder( builder.sort(param, down) );
    }
    
    /**
     * Add a block of SPARQL BGP early in the query.
     * Prefixes in the query text will be expanded 
     */
    public void inject(Call call, String inject) {
        call.updateQueryBuilder( (QueryBuilder qb) -> ((SparqlQueryBuilder)qb).inject(inject) );
    }
  
    /**
     * Add a block of SPARQL BGP late in the query, especially useful for adding filters 
     * Prefixes in the query text will be expanded 
     */
    public void filter(Call call, String filter) {
        call.updateQueryBuilder( (QueryBuilder qb) -> ((SparqlQueryBuilder)qb).filter(filter) );
    }
    
    // ---- Helpers for returning results ---------------------------------

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
        ResponseBuilder builder = startOKBuilder(entity, maxAge);
        if (entity instanceof ResultOrStream) {
            if ( ((ResultOrStream)entity).getCall().getTemplateName() == null || api.isHtmlNonDefault()) {
                // No HTML rendering possible, so perform dynamic content negotiation amongst the rest
                Variant preferred = containerRequest.selectVariant(nonHtmlVariants);
                if (preferred == null) {
                    if (api.isHtmlNonDefault()) {
                        // No alternative to HTML to fall through to default
                    } else {
                        throw new WebApiException(Status.NOT_ACCEPTABLE, "Cannot provide that media type");
                    }
                } else {
                    return builder.type(preferred.getMediaType()).build();
                }
            } else if ( api.isHtmlPreferred() ) {
                // HMTL render possible and preferred, override variant processing to cope with cases like IE8
                Variant preferred = containerRequest.selectVariant(htmlVariants);
                if (preferred != null) {
                    return builder.type(preferred.getMediaType()).build();
                }
            }
        }
        // Let normal message body lookup decide the best rendering
        return builder.build();     
    }
    
    private ResponseBuilder startOKBuilder(Object entity, int maxAge) {
        CacheControl cc = new CacheControl();
        cc.setMaxAge(maxAge);
        ResponseBuilder builder = Response.ok(entity).cacheControl(cc);
        Date lastModified = getLastModifedIfAvailable();
        if (lastModified != null) {
            builder = builder.lastModified(lastModified);
        }
        return builder;
    }
    
    /**
     * Return the given entity with a filename in the content disposition header.
     * Uses defaul maxAge and default conneg (assumes the endpoint a suitable @Produces)
     */
    public Response respondAs(Object entity, String downloadName) {
        CacheControl cc = new CacheControl();
        cc.setMaxAge( (int)getAPI().getMaxAge() );
        return Response.ok(entity)
                .cacheControl(cc)
                .header(CONTENT_DISPOSITION_HEADER, String.format(CONTENT_DISPOSITION_FMT, downloadName))
                .build();     
    }
    
    /**
     * Return a "no cache" cache control setting
     */
    public CacheControl noCache() {
        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        return cc;
    }
    
    // ---- Helpers for Velocity rendering ---------------------------------

    // TODO review context setting
    
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
          
}
