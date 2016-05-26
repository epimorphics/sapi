/******************************************************************
 * File:        QueryBuilder.java
 * Created by:  Dave Reynolds
 * Created on:  30 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;

import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ViewPath;
import com.epimorphics.sparql.geo.GeoQuery;

/**
 * Represents a generic list query (might be SPARQL or noSQL) that can
 * be modified and extended.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface ListQueryBuilder extends QueryBuilder {

    /**
     * Add an equality filter constraint to the query.
     * Use ViewPath version of the call for preference but there are situations where this is the only approach.
     */
    public ListQueryBuilder filter(String shortname, RDFNode value);

    /**
     * Add a one-of filter constraint to the query
     * Use ViewPath version of the call for preference but there are situations where this is the only approach.
     */
    public ListQueryBuilder filter(String shortname, Collection<RDFNode> values);

    /**
     * Add an equality filter constraint to the query
     */
    public ListQueryBuilder filter(ViewPath path, ViewMap map, RDFNode value);

    /**
     * Add a one-of filter constraint to the query
     */
    public ListQueryBuilder filter(ViewPath path, ViewMap map, Collection<RDFNode> values);
    
    // TODO more generalized filters?
    
    /**
     * Add a sort directive to the query
     */
    public ListQueryBuilder sort(String shortname, boolean down);
    
    /**
     * Set a paging window on the query results
     */
    public ListQueryBuilder limit(long limit, long offset);
    
    /**
     * Set a geoquery filter
     */
    public ListQueryBuilder geoQuery(GeoQuery gq);
    
    
    /**
     * Finalize the query, in the case of SPARQL this will include
     * setting prefix bindings.
     */
    public ListQuery build();
}
