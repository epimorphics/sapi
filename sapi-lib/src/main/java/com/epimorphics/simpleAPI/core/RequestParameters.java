/******************************************************************
 * File:        RequestParameters.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.rdfutil.QueryUtil;
import com.epimorphics.util.EpiException;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * <p>Encapsulates the request parameters. This can be used to bind a query template
 * to a concrete query.</p>
 * <p>The request parameters comprise:
 * <ul>
 *   <li>the target URI (after mapping from request URI to the API base URI)</li>
 *   <li>any query/path parameters</li>
 *   <li>any externally injected query filter and modifier clauses</li>
 *   <li>limit/offset from parameters or externally set</li>
 * </ul>
 * </p>
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class RequestParameters {
    public static final String INJECT_MARKER = "#$INJECT$";
    public static final String FILTER_MARKER = "#$FILTER$";
    public static final String MODIFIER_MARKER = "#$MODIFIER$";
    
    public static final String LIMIT_PARAM  = "_limit";
    public static final String OFFSET_PARAM = "_offset";

    protected String uri;
    protected Map<String, Object> bindings = new HashMap<String, Object>();
    protected String filterClause = "";
    protected String injectClause = "";
    protected String modifier = "";
    protected Integer limit;
    protected Integer offset;
    
    public RequestParameters(String uri) {
        this.uri = uri;
    }
    
    public RequestParameters addParameter(String parameter, Object value) {
        bindings.put(parameter, value);
        if (parameter.equals(LIMIT_PARAM)) {
            limit = safeMin(limit, safeAsInt(value));
        }
        if (parameter.equals(OFFSET_PARAM)) {
            offset = safeAsInt(value);
        }
        return this;
    }
    
    public void removeParameter(String parameter) {
        bindings.remove(parameter);
    }
    
    private Integer safeAsInt(Object value) {
        if (value instanceof Number) {
            return ((Number)value).intValue();
        }
        try {
            return Integer.parseInt((String)value);
        } catch (NumberFormatException e) {
            throw new WebApiException(Status.BAD_REQUEST, "Illegal parameter format");
        }
    }
    
    private Integer safeMin(Integer current, Integer value) {
        if (current == null) {
            return value;
        } else {
            return Math.min(current.intValue(), value.intValue());
        }
    }
    
    public RequestParameters addParameters(UriInfo info) {
        addParameters( info.getPathParameters() );
        addParameters( info.getQueryParameters() );
        return this;
    }
    
    protected RequestParameters addParameters(MultivaluedMap<String, String> params) {
        for (String key : params.keySet()) {
            addParameter(key, params.getFirst(key));
        }
        return this;
    }
    
    public RequestParameters addFilter(String filterClause) {
        this.filterClause += " " + filterClause;
        return this;
    }
    
    public RequestParameters addInject(String injectClause) {
        this.injectClause += " " + injectClause;
        return this;
    }
    
    public RequestParameters addModifier(String modifier) {
        this.modifier += " " + modifier;
        return this;
    }
    
    public void setLimit(int lmt) {
        limit = safeMin(limit, lmt);
    }
    
    public void setSoftLimit(int lmt) {
        if (limit == null) {
            limit = lmt;
        }
    }

    public Integer getLimit() {
        return limit;
    }
    
    public Integer getOffset() {
        return offset;
    }
    
    public String getUri() {
        return uri;
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }
    
    public Object getBinding(String key) {
        return bindings.get(key);
    }
    
    public boolean hasParameter(String key) {
        return bindings.containsKey(key);
    }
    
    public boolean hasBindingFor(String key) {
        if (hasParameter(key)) {
            Object value = getBinding(key);
            if (value != null) {
                if (value instanceof String) {
                    return ! ((String)value).isEmpty();
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public String getFilterClause() {
        return filterClause;
    }
    
    /**
     * Bind the query by injecting any offset/limits settings, explicit filters and other modifiers, and the parameter values.
     * @param query
     * @return
     */
    public String bindQuery(String query) {
        String q = query;
        if (limit != null) {
            modifier += " LIMIT " + limit;
        }
        if (offset != null) {
            modifier += " OFFSET " + offset;
        }
        q = bindMarker(q, FILTER_MARKER, filterClause);
        q = bindMarker(q, INJECT_MARKER, injectClause);
        q = bindMarker(q, MODIFIER_MARKER, modifier);
        return q;
    }
    
    public String bindQueryAndID(String query) {
        return bindQueryID( bindQuery(query) );
    }

    /**
     * Bind the ?id variable in the query to the requested URI
     */
    public String bindQueryID(String query) {
        return bindQueryParam(query, "id", ResourceFactory.createResource(uri) );
    }

    public String bindQueryParam(String query, String param) {
        Object value = getBinding(param);
        if (value == null) {
            return query;
        } else {
            return bindQueryParam(query, param, value);
        }
    }
    
    public static String bindQueryParam(String query, String var, Object value) {
        String subs = QueryUtil.asSPARQLValue( value ).replace("\\", "\\\\");
        // Two step substitute so don't use regex when substituting value (which might have regex special characters)
        String bound = query.replaceAll("\\?" + var + "\\b", MARKER);
        bound = bound.replace(MARKER, subs);
        return bound;
    }
    protected static final String MARKER="?ILLEGAL-VAR";

    protected String bindMarker(String query, String marker, String value) {
        if (value != null && !value.isEmpty()) {
            if (query.contains(marker)) {
                return query.replace(marker, value);
            } else {
                throw new EpiException("No " + marker + " marker in query, can't inject. Query was: " + query);
            }
        }
        return query;
    }
    
    public Long getSafeIntParam(String param) {
        Object value = bindings.get(param);
        if (value != null) {
            if (value instanceof Number) {
                return ((Number)value).longValue();
            } else if (value instanceof String) {
                try {
                    return Long.parseLong((String)value);
                } catch (NumberFormatException e) {
                    throw new WebApiException(Status.BAD_REQUEST, "Illegal parameter format");
                }
            }
        }
        return null;
    }
    
    public Double getSafeDoubleParam(String param) {
        Object value = bindings.get(param);
        if (value != null) {
            if (value instanceof Number) {
                return ((Number)value).doubleValue();
            } else if (value instanceof String) {
                try {
                    return Double.parseDouble((String)value);
                } catch (NumberFormatException e) {
                    throw new WebApiException(Status.BAD_REQUEST, "Illegal parameter format");
                }
            }
        }
        return null;
    }

    public String getSafeStringParam(String param) {
        Object value = bindings.get(param);
        if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }
    
    /**
     * Inject a filter for geo bounding box using OSGB grid coordinates.
     * Assumes the var items have sr:easting/sr:northing values.
     * Assumes variables ?eee and ?nnn are free for reuse in the query.
     * 
     * @param easting easting of centre of box 
     * @param northing northing of centre of box
     * @param radius radius of box in m
     */
    public void injectENBox(String var, long easting, long northing, long radius) {
        injectENBox(var, easting - radius, easting + radius, northing + radius, northing - radius);
    }
    
    /**
     * Inject a filter for geo bounding box using OSGB grid coordinates.
     * Assumes the items have sr:easting/sr:northing values.
     * Assumes variables ?eee and ?nnn are free for reuse in the query.
     * 
     * @param left easting of left hand boundary
     * @param right easting of right hand boundary
     * @param top northing of top boundary
     * @param bottom northing of botton boundary
     */
    public void injectENBox(String var, long left, long right, long top, long bottom) {
        addFilter( String.format(BOX_FILTER, var, left, right, top, bottom) );
    }
    
    
    protected final String BOX_FILTER  = 
            "?%s <http://data.ordnancesurvey.co.uk/ontology/spatialrelations/easting> ?eee; "
            + "<http://data.ordnancesurvey.co.uk/ontology/spatialrelations/northing> ?nnn .\n"
            + "FILTER (?eee >= %d && ?eee <= %d && ?nnn <= %d && ?nnn >= %d)";
}

