/******************************************************************
 * File:        ResultStreamSparqlSelect.java
 * Created by:  Dave Reynolds
 * Created on:  29 Sep 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results;

import java.util.Iterator;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.epimorphics.appbase.data.ClosableResultSet;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.requests.Request;
import com.epimorphics.simpleAPI.views.ViewEntry;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ViewTree;
import com.epimorphics.util.EpiException;

/**
 * A ResultStream based on a streaming ResultSet from a base SPARQL query mapped
 * to a series of trees values according to some ViewMap.
 * The query rows are assumed to have a distinguished "id" variable representing
 * the root resource. Neighbouring rows in the ResultSet with the same id
 * are coalesced.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ResultStreamSparqlSelect implements ResultStream {
    protected ResultSet results;
    protected EndpointSpec spec;
    protected QuerySolution nextRow;
    protected Resource nextID;
    protected Request request;
    
    public ResultStreamSparqlSelect(ResultSet resultSet, EndpointSpec spec, Request request) {
        this.results = resultSet;
        this.spec = spec;
        this.request = request;
    }
    
    @Override
    public Request getRequest() {
        return request;
    }
    
    @Override
    public EndpointSpec getSpec() {
        return spec;
    }

    public ViewMap getView() {
        return spec.getView();
    }

    @Override
    public Iterator<Result> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return nextID != null || results.hasNext();
    }

    @Override
    public Result next() {
        if (hasNext()) {
            try {
                if (nextRow == null) {
                    nextRow = results.next();
                    nextID = nextRow.getResource("id");
                }
                Result result = new Result(this, nextID);
                Resource target = nextID;
                while (nextID != null && nextID.equals(target)) {
                    addRow(result, nextRow);
                    if (results.hasNext()) {
                        nextRow = results.next();
                        nextID = nextRow.getResource("id");
                    } else {
                        nextID = null;
                        close();
                    }
                }
                return result;
            } catch (Exception e) {
                // Assume exceptions are fatal and clean up higher up
                // TODO Does java8 have better mechanisms for things like this?
                close();
                throw new EpiException("Results stream aborted", e);
            }
        } else {
            return null;
        }
    }
    
    private void addRow(Result result, QuerySolution row) {
        if (getView() == null) {
            for (Iterator<String> vi = row.varNames(); vi.hasNext();) {
                String var = vi.next();
                if (!var.equals("id")) {
                    result.add(var, row.get(var));
                }
            }
        } else {
            addTree(result, getView().getTree(), row, "");
        }
    }
    
    private void addTree(Result result, ViewTree tree, QuerySolution row, String path) {
        for (ViewEntry ve : tree) {
            String key = ve.getJsonName();
            String npath = path.isEmpty() ? key : path + "_" + key;
            RDFNode value = row.get( npath );
            if (value != null) {
                if (ve.isNested()) {
                    Result nested = result.getNested(key, value);
                    if (nested == null) {
                        nested = new Result(this, value);
                        result.add(key, nested);
                    }
                    addTree(nested, ve.getNested(), row, npath);
                } else {
                    result.add(key, value);
                }
            }
        }
    }
    
    public void close() {
        if (results instanceof ClosableResultSet) {
            ((ClosableResultSet)results).close();
        }
    }
}
