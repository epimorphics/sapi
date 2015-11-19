/******************************************************************
 * File:        RequestCheck.java
 * Created by:  Dave Reynolds
 * Created on:  19 Nov 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.requests;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.appbase.webapi.WebApiException;

/**
 * Utility for validating a request against a set of externally configuable
 * parameter constraints.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class RequestCheck {
    protected List<ParameterCheck> checks = new ArrayList<>();
    
    public RequestCheck() {
    }
    
    public void addParameterCheck(ParameterCheck check) {
        checks.add( check );
    }
    
    public List<ParameterCheck> getChecks() {
        return checks;
    }
    
    public static RequestCheck fromJson(JsonObject json) {
        RequestCheck check = new RequestCheck();
        for (String param : json.keys()) {
            check.addParameterCheck( ParameterCheck.fromJson(param, json.get(param)) );
        }
        return check;
    }
    
    /**
     * Checks the given request against the parameter constraints.
     * May side-effect the request parameters to add in default values.
     * @throws WebApiException if a check fails
     */
    public void checkRequest(Request request) throws WebApiException {
        StringBuffer errors = new StringBuffer();
        boolean ok = true;
        for (ParameterCheck check : checks) {
            String error = check.checkAndReport(request);
            if (error != null) {
                errors.append(error);
                errors.append("\n");
                ok = false;
            }
        }
        if (!ok) {
            throw new WebApiException(Status.BAD_REQUEST, errors.toString());
        }
    }
    
}
