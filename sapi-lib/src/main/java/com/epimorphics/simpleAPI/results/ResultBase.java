/******************************************************************
 * File:        ResultBase.java
 * Created by:  Dave Reynolds
 * Created on:  9 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.results;

import java.io.ByteArrayOutputStream;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.rdf.model.Resource;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.requests.Request;

/**
 * Handy base class for implementing Result
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public abstract class ResultBase implements Result {
    protected Call call;
    
    public ResultBase(Call call) {
        this.call = call;
    }
    
    @Override
    public Call getCall() {
        return call;
    }

    @Override
    public EndpointSpec getSpec() {
        return call.getEndpoint();
    }

    @Override
    public Request getRequest() {
        return call.getRequest();
    }

    public abstract Resource asResource();

    public abstract void writeJson(JSFullWriter out);

    /**
     * Return result formatted as a JSON object.
     * Warning this is current a slow implementation (serialize then re-parse) since
     * it is only used for testing. If used in production then should re-implement 
     */
    @Override
    public JsonObject asJson() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JSFullWriter out = new JSFullWriter(bos);
        out.startOutput();
        writeJson(out);
        out.finishOutput();
        
        return JSON.parse(bos.toString());        
    }
    
}
