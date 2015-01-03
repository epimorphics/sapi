/******************************************************************
 * File:        NodeWriterPolicy.java
 * Created by:  Dave Reynolds
 * Created on:  9 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Encapsulates policy for how to serialize RDFNodes to Json.
 * Implementations may support prefix mapping, short name expansion, 
 * flagging of structured properties, blocking of language tagging etc.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public interface NodeWriterPolicy {

    public String keyFor(Property prop);
    
    public String uriValue(Resource value);
    
    public boolean showLangTag(String key, String lang);
    
    public boolean allowNesting(String key);
}
