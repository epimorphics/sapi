/******************************************************************
 * File:        DefaultHandler.java
 * Created by:  Dave Reynolds
 * Created on:  6 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi.basetest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.epimorphics.simpleAPI.webapi.EndpointsBase;

/**
 * Handle any request that aren't handled by more specific paths. 
 * This invokes the default processing machinery to lookup dynamic endpoints.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
@Path("/{__path:.*}")
public class DefaultHandler extends EndpointsBase {

    @GET
    public Response handleDefault() {
        return defaultResponse();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleDefault(String body) {
        return defaultResponse(body);
    }
}
