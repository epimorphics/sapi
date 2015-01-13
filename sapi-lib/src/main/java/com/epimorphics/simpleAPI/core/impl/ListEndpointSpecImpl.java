/******************************************************************
 * File:        SelectEndpointSpecImpl.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.data.ClosableResultSet;
import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.rdfutil.QueryUtil;
import com.epimorphics.rdfutil.TypeUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.JSONNodeDescription;
import com.epimorphics.simpleAPI.core.ListEndpointSpec;
import com.epimorphics.simpleAPI.core.RequestParameters;
import com.epimorphics.simpleAPI.writers.JsonWriterUtil;
import com.epimorphics.simpleAPI.writers.ValueStream;
import com.hp.hpl.jena.query.ResultSet;

public class ListEndpointSpecImpl extends EndpointSpecBase implements ListEndpointSpec {
    static final Logger log = LoggerFactory.getLogger( ListEndpointSpecImpl.class );
            
    protected String baseQuery;
    protected String query;
    protected List<String> bindingVars = new ArrayList<>();
    protected Integer hardLimit;
    
    public ListEndpointSpecImpl(API api, JsonObject config) {
        super(api, config);
    }

    public void setBaseQuery(String query) {
        this.baseQuery = query;
    }
    
    public void setHardLimit(int limit) {
        this.hardLimit = limit;
    }
    
    @Override
    public String getQuery(RequestParameters request) {
        if (query == null) {
            if (rawQuery == null) {
                rawQuery = map.asQuery(baseQuery);
            }
            query = expandPrefixes( rawQuery );
        }
        String q = bindVars(request, query);
        injectFilters(request);
        if (hardLimit != null) {
            request.setLimit(hardLimit);
        }
        return request.bindQuery(q);
    }
    
    protected void injectFilters(RequestParameters request) {
        for (String param : request.getBindings().keySet()) {
            if (!param.startsWith("_")) {
                JSONNodeDescription entry = map.getEntry(param);
                if (entry.isFilterable()) {
                    Object value = request.getBinding(param);
                    if (value != null) {
                        if (value instanceof String) {
                            try {
                                String typeURI = entry.getType();
                                if (typeURI != null) {
                                    typeURI = getPrefixes().expandPrefix(typeURI);
                                }
                                value = TypeUtil.asTypedValue((String)value, typeURI);
                            } catch (Exception e) {
                                throw new WebApiException(Status.BAD_REQUEST, "Illegal value for parameter " + param);
                            }
                        }
                        request.addFilter( String.format("FILTER( ?%s = %s )", param, QueryUtil.asSPARQLValue(value)) );
                    }
                } else {
                    log.warn("Unrecognized query parameter: " + param);
                    // TODO return the request?
                }
            }
        }
    }
    
    protected String bindVars(RequestParameters request, String query) {
        String q = query;
        for (String param : bindingVars) {
            q = request.bindQueryParam(query, param);
        }
        return q;
    }

    @Override
    public JSONWritable getWriter(ResultSet results, RequestParameters request) {
        return new Writer(results, request);
    }
    
    public void addBindingParam(String param) {
        bindingVars.add(param);
    }

    @Override
    public List<String> listBindingParams() {
        return bindingVars;
    }
    
    public class Writer implements JSONWritable {
        ValueStream values;
        RequestParameters request;
        ResultSet results;
        
        public Writer(ResultSet results, RequestParameters request) {
            this.values = new ValueStream(results, map);
            this.request = request;
            this.results = results;
        }
        
        @Override
        public void writeTo(JSFullWriter out) {
            try {
                out.startObject();
                api.writeMetadata(out);
                if (request.getLimit() != null) {
                    out.pair("limit", request.getLimit());
                }
                if (request.getOffset() != null) {
                    out.pair("offset", request.getOffset());
                }
                out.key( getItemName() );
                out.startArray();
                while (values.hasNext()) {
                    out.arrayElementProcess();
                    JsonWriterUtil.writeValueSet(map, values.next(), out);
                }
                out.finishArray();
                out.finishObject();
            } finally {
                if (results instanceof ClosableResultSet) {
                    try {
                        ((ClosableResultSet)results).close();
                    } catch (Exception e) {
                        // ignore, probably already closed
                    }
                }
            }
        }
    }

}
