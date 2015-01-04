/******************************************************************
 * File:        ModelJsonWriter.java
 * Created by:  Dave Reynolds
 * Created on:  9 Dec 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.core.APIConfig;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A streaming json output for a single resource.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ResourceJsonWriter implements JSONWritable {
    protected Resource root;
    protected APIConfig config;
    protected String itemName;
    
    public ResourceJsonWriter(APIConfig config, String itemName, Resource root) {
        this.root = root;
        this.config = config;
        this.itemName = itemName;
    }
    
    public ResourceJsonWriter(APIConfig config, Resource root) {
        this(config, "item", root);
    }
    
    public ResourceJsonWriter(API api, String itemName, Resource root) {
        this(api.getDefaultConfig(), itemName, root);
    }
    
    public ResourceJsonWriter(API api, Resource root) {
        this(api, "item", root);
    }

    @Override
    public void writeTo(JSFullWriter out) {
        out.startObject();
        config.getAPI().writeMetadata(out);
        out.key(itemName);
        KeyValueSet values = KeyValueSet.fromResource(config, root);
        JsonWriterUtil.writeKeyValues(config, values, out);
        out.finishObject();
    }
}
