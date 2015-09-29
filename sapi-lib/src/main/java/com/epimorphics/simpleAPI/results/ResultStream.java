/******************************************************************
 * File:        KeyValueSetStream.java
 * Created by:  Dave Reynolds
 * Created on:  10 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results;

import java.util.Iterator;

import com.epimorphics.simpleAPI.views.ViewMap;

/**
 * Represents a stream of query results. Each result is a tree view over the underlying
 * data according to some associatd ViewMap.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface ResultStream extends Iterable<Result>, Iterator<Result> {

    public ViewMap getView();
    
}
