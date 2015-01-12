/******************************************************************
 * File:        KeyValueSetStream.java
 * Created by:  Dave Reynolds
 * Created on:  10 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import java.util.Iterator;

import com.epimorphics.simpleAPI.core.JSONMap;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Wraps a SPARQL ResultSet and transforms it to a streamable series
 * of KeyValueSet entries by coalescing neighbouring result rows about
 * the sample entity. Relies or sort in the query, or "natural"
 * grouping of query processor results, no separate sorting at this stage.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ValueStream implements Iterable<ValueSet>, Iterator<ValueSet> {
    protected ResultSet results;
    protected QuerySolution nextRow;
    protected Resource nextID;
    protected JSONMap map;
    
    public ValueStream(ResultSet results, JSONMap map) {
        this.results = results;
        this.map = map;
    }
    
    public ValueStream(ResultSet results) {
    this.results = results;
    }

    @Override
    public Iterator<ValueSet> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return nextID != null || results.hasNext();
    }

    @Override
    public ValueSet next() {
        if (hasNext()) {
            if (nextRow == null) {
                nextRow = results.next();
                nextID = nextRow.getResource("id");
            }
            ValueSet values = new ValueSet(nextID);
            Resource target = nextID;
            while (nextID != null && nextID.equals(target)) {
                values.addRow(nextRow, map);
                if (results.hasNext()) {
                    nextRow = results.next();
                    nextID = nextRow.getResource("id");
                } else {
                    nextID = null;
                }
            }
            return values;
        } else {
            return null;
        }
    }

    @Override
    public void remove() {
        throw new  UnsupportedOperationException();
    }
    
}
