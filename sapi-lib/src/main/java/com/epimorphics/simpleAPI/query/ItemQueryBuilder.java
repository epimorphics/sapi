/******************************************************************
 * File:        QueryBuilder.java
 * Created by:  Dave Reynolds
 * Created on:  30 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.query;

/**
 * Represents a generic item query (might be SPARQL or noSQL) that can
 * be modified and extended.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface ItemQueryBuilder extends QueryBuilder {

    /**
     * Finalize the query, in the case of SPARQL this will include
     * setting prefix bindings.
     */
    public ItemQuery build();
}
