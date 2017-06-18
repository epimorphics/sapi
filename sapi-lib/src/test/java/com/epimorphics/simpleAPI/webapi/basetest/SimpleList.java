/******************************************************************
 * File:        SimpleList.java
 * Created by:  Dave Reynolds
 * Created on:  5 Oct 2015
 * 
 * (c) Copyright 2015, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.webapi.basetest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.epimorphics.simpleAPI.query.QueryBuilder;
import com.epimorphics.simpleAPI.query.impl.SparqlQueryBuilder;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.requests.Request;
import com.epimorphics.simpleAPI.webapi.EndpointsBase;

@Path("basetest")
public class SimpleList extends EndpointsBase {

    @GET
    @Path("list")
    @Produces({MediaType.APPLICATION_JSON, TURTLE, CSV, MediaType.TEXT_HTML})
    public Response listTest2() {
        return listResponse( getRequest(), "listTest2");
    }

    @GET
    @Path("listSuppress")
    @Produces({MediaType.APPLICATION_JSON, TURTLE, CSV, MediaType.TEXT_HTML})
    public Response listTestSuppressed() {
        return listResponse( getRequest(), "listTestSuppressed");
    }

    @GET
    @Path("listNested")
    @Produces({MediaType.APPLICATION_JSON, TURTLE, CSV, MediaType.TEXT_HTML})
    public Response listNested() {
        return listResponse( getRequest(), "listTestNest");
    }

    public static final String FILTER_PARAM = "filter";
    @GET
    @Path("listNestedSelect")
    @Produces({MediaType.APPLICATION_JSON, TURTLE, CSV, MediaType.TEXT_HTML})
    public Response listNestedSelect() {
        Request request = getRequest();
        Call call = new Call(getAPI(), "listNestedSelect", request);
        if ( request.hasAvailableParameter(FILTER_PARAM) ) {
            String filter = " { ?id egn:group ?group . FILTER(?group = '%s')} ".replace("%s", request.getFirst(FILTER_PARAM));
            request.consume(FILTER_PARAM);
            call.updateQueryBuilder( (QueryBuilder qb) -> ((SparqlQueryBuilder)qb).filter(filter) );
        }
        return respondWith( call.getResults() );
    }

    @GET
    @Path("listGeo")
    @Produces({MediaType.APPLICATION_JSON, TURTLE, CSV, GEO_JSON})
    public Response listGeo() {
        return listResponse( getRequest(), "listGeo");
    }

    @POST
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listTest2Post(String body) {
        return listResponse( getRequest(body), "listTest2");
    }
    
}
