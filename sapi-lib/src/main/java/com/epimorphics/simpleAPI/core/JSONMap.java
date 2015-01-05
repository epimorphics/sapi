/******************************************************************
 * File:        JSONMap.java
 * Created by:  Dave Reynolds
 * Created on:  5 Jan 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.core;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Property;

/**
 * Represents a mapping between RDF values and a JSON representation.
 */
public interface JSONMap {

    /**
     * Return the JSON writer policy to use for the given json key
     */
    public JSONNodePolicy policyFor(String key);
    
    /**
     * Return the JSON key to use when rendering the given RDF property.
     * Only relevant when serializing descriptions as opposed to SELECT result sets.
     */
    public String keyFor(Property property);

    /**
     * Return a list of top level JSON keys to render (if present in the data).
     * Used when serializing SELECT results sets to guide the recursive nesting.
     * Can return null if all values are to be rendered.
     */
    public List<String> listKeys(); 
}
