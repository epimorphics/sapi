/******************************************************************
 * File:        ListEndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  29 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.endpoints.impl;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.ListEndpointSpec;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.query.impl.SparqlQueryBuilder;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.sparql.query.Query;

/**
 * Endpoints which return lists of results and can have associated hard/soft limits.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class SparqlListEndpointSpec extends SparqlEndpointSpec implements ListEndpointSpec {
    protected Long softLimit;
    protected Long hardLimit;
    
    public SparqlListEndpointSpec(API api) {
        super(api);
    }

    @Override public QueryBuilder getQueryBuilder(String viewname) {
        ViewMap view = getView(viewname);
        Query q = baseQuery.copy();
        if (view != null) view.addTreePattern(q);
        return SparqlQueryBuilder.fromBaseQuery(q, getPrefixes());
    }

    /**
     * Return a soft limit value of the number of results allowed.
     * If the query does not state a limit this soft limit is used. 
     * The query is allowed to override this and return more results
     * May be null if no soft limit has been specified.
     */
    public Long getSoftLimit() {
        return softLimit;
    }

    /**
     * Return a hard limit value of the number of results allowed.
     * The number of results allow can be no more than this hard limit,
     * but the query can give a lower limit.
     * May be null if no hard limit has been specified.
     */
    public Long getHardLimit() {
        return hardLimit;
    }

    public void setSoftLimit(long softLimit) {
        this.softLimit = softLimit;
    }

    public void setHardLimit(long hardLimit) {
        this.hardLimit = hardLimit;
    }
    
}
