/******************************************************************
 * File:        KeyValueSet.java
 * Created by:  Dave Reynolds
 * Created on:  9 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import com.epimorphics.json.JSFullWriter;

/**
 * Abstraction for a single result in a result stream. 
 * Each result describes some resource with some associated tree or graph of values and nested resources.
 * This abstraction allows us to use either RDF or some JSON-like tree representation underneath.
 *  
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */

// Note. Original had result as always the JSON tree but for DESCRIBES over a real triple store
// when returning in RDF form we do want arbitrary graphs without having to go in and out of the tree format.

public interface Result extends ResultOrStream {
    
    /**
     * Return the result as an RDF resource for serialization to RDF formats
     */
    public Resource asResource();
    
    /**
     * Return the result as an RDF resource for serialization to RDF formats.
     * Copied into the given model
     */
    public Resource asResource(Model model);
    
    /**
     * Render the result to a JSON stream
     */
    public void writeJson(JSFullWriter out);

    
    /**
     * Return result formatted as a JSON object.
     */
    public JsonObject asJson();

    // TODO CSV rendering
}
