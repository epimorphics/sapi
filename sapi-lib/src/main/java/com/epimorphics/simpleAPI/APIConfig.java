/******************************************************************
 * File:        APIConfig.java
 * Created by:  Dave Reynolds
 * Created on:  23 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Represents configuration information for a single API endpoint which is loaded
 * from a YAML file via the API component. Elements include:
 * <ul>
 *  <li></li>
 *  <li></li>
 *  <li></li>
 * </ul>
 */

//TODO elements
// - name
// - original JSON as metadata
// - explicit query
// - query type select/construct/describe/implicit
// - json mapping (nesting, optional, multivalued)
// - URI template
// - html template

// pointer to parent API

public class APIConfig implements NodeWriterPolicy {
    protected API api;
    
    /**
     * Construct an empty default config for a given API instance.
     */
    public APIConfig(API parent) {
        this.api = parent;
    }

    // ---- Implements NodeWriter Policy --------------------------------

    @Override
    public String keyFor(Property prop) {
        return api.shortnameFor(prop);
    }

    @Override
    public String uriValue(Resource value) {
        return value.getURI();
    }

    @Override
    public boolean showLangTag(String key, String lang) {
        return true;
    }

    @Override
    public boolean allowNesting(String key) {
        return true;
    }
}
