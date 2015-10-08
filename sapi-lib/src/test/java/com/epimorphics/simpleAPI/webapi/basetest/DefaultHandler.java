/******************************************************************
 * File:        DefaultHandler.java
 * Created by:  Dave Reynolds
 * Created on:  6 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi.basetest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.epimorphics.simpleAPI.webapi.EndpointsBase;
import com.epimorphics.simpleAPI.results.ResultStream;

/**
 * Handle any request that aren't handled by more specific paths. 
 * This invokes the default processing machinery to lookup dynamic endpoints.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
@Path("/{path:.*}")
public class DefaultHandler extends EndpointsBase {

    @GET
    public ResultStream handleDefault() {
        return defaultResponse();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultStream handleDefault(String body) {
        return defaultResponse(body);
    }
}
