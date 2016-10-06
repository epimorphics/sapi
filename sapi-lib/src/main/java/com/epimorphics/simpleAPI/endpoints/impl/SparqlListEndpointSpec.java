/******************************************************************
 * File:        ListEndpointSpec.java
 * Created by:  Dave Reynolds
 * Created on:  29 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.endpoints.impl;

import java.util.List;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.ListEndpointSpec;
import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.query.impl.NestedSparqlQueryBuilder;
import com.epimorphics.simpleAPI.query.impl.SparqlQueryBuilder;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.sparql.query.Distinction;
import com.epimorphics.sparql.query.QueryShape;

/**
 * Endpoints which return lists of results and can have associated hard/soft limits.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class SparqlListEndpointSpec extends SparqlEndpointSpec implements ListEndpointSpec {
    protected Long softLimit;
    protected Long hardLimit;
    protected boolean useNestedSelect = false;
    protected boolean useDistinct = false;
    protected List<String> additionalProjectionVars = null;
    
    public SparqlListEndpointSpec(API api) {
        super(api);
    }
    
    @Override public QueryBuilder getQueryBuilder(String viewname) {
        ViewMap view = getView(viewname);
        QueryShape base = getBaseQuery().copy();
        if (useDistinct) {
            base.setDistinction(Distinction.DISTINCT);
        }
        if ( useNestedSelect ) {
            QueryShape outerQuery = new QueryShape();
            if (view != null) {
                outerQuery.addLaterPattern( view.asPattern() );
            }
            return new NestedSparqlQueryBuilder( base, outerQuery, getPrefixes(), additionalProjectionVars );
        } else {
            if (view != null) {
                view.injectTreePatternInfo(base);
            }
            return SparqlQueryBuilder.fromBaseQuery(base, getPrefixes());
        }
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
    
    /**
     * Set to true to force use of nested selects in SPARQL query creation for this endpoint
     */
    public void setUseNestedSelect(boolean useNestedSelect) {
        this.useNestedSelect = useNestedSelect;
    }
    
    
    public boolean useNestedSelect() {
        return useNestedSelect;
    }
    
    /**
     * Define a set of additional variables that should be projected from a nested select
     */
    public void setAdditionalProjectionVars(List<String> vars) {
        additionalProjectionVars = vars;
    }

    public boolean isUseDistinct() {
        return useDistinct;
    }

    /**
     * Set to true to add a DISTINCT qualifier to the query (or the inner query in the case of a nested select) 
     */
    public void setUseDistinct(boolean useDistinct) {
        this.useDistinct = useDistinct;
    }
        
    
}
