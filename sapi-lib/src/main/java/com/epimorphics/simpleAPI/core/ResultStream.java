/******************************************************************
 * File:        ResultStream.java
 * Created by:  Dave Reynolds
 * Created on:  28 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

/**
 * A stream of results from a query.
 * An item query will generate only a single result, a list query will generate an arbitrary number.
 * The results will have been mapped from native format (RDF model, SPARQL result set, JSON document)
 * to a merged tree model using some view projection.
 *  
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ResultStream {

    // TODO
}
