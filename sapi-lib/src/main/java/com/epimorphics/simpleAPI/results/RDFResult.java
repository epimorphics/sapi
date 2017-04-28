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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.views.PropertySpec;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ClassSpec;
import com.epimorphics.sparql.terms.URI;

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
    public Resource asResource(Model model) {
        model.add( root.getModel() );
        return root.inModel(model);
    }

    @Override
    public void writeJson(JSFullWriter out) {
        asTreeResult().writeJson(out);
    }
    
    public TreeResult asTreeResult() {
        // TODO if there is a view should we limit the describe to that view?
        ViewMap view = call.getView();
        if (view == null){
            view = call.getAPI().getView(API.DEFAULT_VIEWNAME);
        }
        return fromResource(root, new HashSet<>(), view == null ? null : view.getTree());
    }

    protected TreeResult fromResource(Resource root, Set<Resource> seen, ClassSpec view) {
        seen.add(root);
        TreeResult result = new TreeResult(call, root);
        for (StmtIterator i = root.listProperties(); i.hasNext(); ) {
            Statement s = i.next();
            String pURI = s.getPredicate().getURI();
            PropertySpec pView = (view == null) ? null :  view.findEntryByURI(pURI);
            if (pView == null) {
                pView = new PropertySpec( new URI(pURI) );
            }
            String key = pView.getJsonName();
            RDFNode value = s.getObject();
            if (value.isResource() && !seen.contains(value)) {
                result.add(key, fromResource(value.asResource(), seen, pView.isNested() ? pView.getNested() : view));
            } else {
                result.add(key,  value);
            }
        }
        seen.remove(root);
        return result;
    }
    
}
