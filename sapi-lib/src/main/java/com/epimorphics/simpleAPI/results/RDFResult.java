/******************************************************************
 * File:        RDFResult.java
 * Created by:  Dave Reynolds
 * Created on:  9 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.simpleAPI.requests.Call;

/**
 * A result which is stored as a real RDF (in memory) value.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class RDFResult extends ResultBase implements Result {
    protected Resource root;
    
    public RDFResult(Resource root, Call call) {
        super(call);
        this.root = root;
    }

    @Override
    public Resource asResource() {
        return root;
    }

    @Override
    public void writeJson(JSFullWriter out) {
        asTreeResult().writeJson(out);
    }
    
    public TreeResult asTreeResult() {
        return fromResource(root, new HashSet<>());
    }

    protected TreeResult fromResource(Resource root, Set<Resource> seen) {
        seen.add(root);
        TreeResult result = new TreeResult(call, root);
        for (StmtIterator i = root.listProperties(); i.hasNext(); ) {
            Statement s = i.next();
            String key = getCall().getAPI().getDefaultViewForURI( s.getPredicate().getURI() ).getJsonName();
            RDFNode value = s.getObject();
            if (value.isResource()) {
                result.add(key, fromResource(value.asResource(), seen));
            } else {
                result.add(key,  value);
            }
        }
        seen.remove(root);
        return result;
    }
    
}
