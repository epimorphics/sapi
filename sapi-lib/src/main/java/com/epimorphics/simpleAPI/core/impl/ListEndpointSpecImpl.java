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

import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.rdfutil.QueryUtil;
import com.epimorphics.rdfutil.TypeUtil;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.ListEndpointSpec;
import com.epimorphics.simpleAPI.core.RequestParameters;
import com.epimorphics.simpleAPI.writers.JsonWriterUtil;
import com.epimorphics.simpleAPI.writers.KeyValueSetStream;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.query.ResultSet;

public class ListEndpointSpecImpl extends EndpointSpecBase implements ListEndpointSpec {
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
                if (map instanceof JSONExplicitMap) {
                    rawQuery = ((JSONExplicitMap)map).asQuery(baseQuery);
                } else {
                    throw new EpiException("Cannot query - need either a mapping or an explicit query");
                }
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
        if (map instanceof JSONExplicitMap) {
            for (JSONMapEntry entry : ((JSONExplicitMap)map).mapping) {
                if (entry.isFilterable()) {
                    String var = entry.getJsonName();
                    Object value = request.getBinding(var);
                    if (value != null) {
                        if (value instanceof String) {
                            try {
                                String typeURI = entry.getType();
                                if (typeURI != null) {
                                    typeURI = getPrefixes().expandPrefix(typeURI);
                                }
                                value = TypeUtil.asTypedValue((String)value, typeURI);
                            } catch (Exception e) {
                                throw new WebApiException(Status.BAD_REQUEST, "Illegal value for parameter " + var);
                            }
                        }
                        request.addFilter( String.format("FILTER( ?%s = %s )", var, QueryUtil.asSPARQLValue(value)) );
                    }
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
    public JSONWritable getWriter(KeyValueSetStream results, RequestParameters request) {
        return new Writer(results, request);
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
        KeyValueSetStream values;
        RequestParameters request;
        
        public Writer(KeyValueSetStream values, RequestParameters request) {
            this.values = values;
            this.request = request;
        }
        
        public Writer(ResultSet results, RequestParameters request) {
            this.values = new KeyValueSetStream(results);
            this.request = request;
        }
        
        @Override
        public void writeTo(JSFullWriter out) {
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
                JsonWriterUtil.writeKeyValues(map, values.next(), out);
            }
            out.finishArray();
            out.finishObject();
        }
    }

}
