/******************************************************************
 * File:        JsonWriterUtil.java
 * Created by:  Dave Reynolds
 * Created on:  1 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.results.TreeResult;
import com.epimorphics.simpleAPI.views.PropertySpec;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ClassSpec;
import com.epimorphics.util.EpiException;

/**
 * Serialize a TreeResult as as an RDF model.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class RDFWriterUtil {

    public static Resource writeResult(TreeResult result, Model model) {
        EndpointSpec spec = result.getCall().getEndpoint();
        ViewMap view = result.getCall().getView();
        if (view == null) {
            throw new EpiException("Can't format as RDF output without an explicit view map");
        }
        return writeResult(result, view.getTree(), spec.getAPI(), model);
    }
    
    protected static Resource writeResult(TreeResult result, ClassSpec tree, API api, Model model) {
        Resource root = result.getId().inModel(model).asResource();
        for (String key : result.getKeys()) {
            PropertySpec entry = tree.getEntry(key);
            Property prop = model.createProperty( entry.getProperty().getURI() );
            for (Object value : result.getValues(key)) {
                if (value instanceof TreeResult) {
                    if (entry.isNested()) {
                        Resource sub = writeResult((TreeResult)value, entry.getNested(), api, model);
                        root.addProperty(prop, sub);
                    } else {
                        throw new EpiException("Inconsitency between view and result tree structure");
                    }
                } else if (value instanceof RDFNode) {
                    root.addProperty(prop, (RDFNode)value);
                }
            }
        }
        return root;
    }

}
