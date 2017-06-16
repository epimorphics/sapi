/******************************************************************
 * File:        GeojsonWriter.java
 * Created by:  Dave Reynolds
 * Created on:  16 Jun 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.results.TreeResult;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ViewPath;

/**
 * Support for serializing result (stream) as GeoJSON feature (featureCollection).
 * <p>
 * The geometry is assumed to be already assembled as a geojson string literal 
 * and attached to the result through a configurable property (set value for
 * geometryProp on endpoint spec).</p>
 * <p>
 * The "properties" field of the geojson can take one of two forms. If there is a
 * CSVMap specified for the view then a properties object is contructed isomorphic
 * to the CSVMap rendering. Otherwise the properties object is the whole JSON render of the
 * results (less the geometry property itself).</p>
 * <p>
 * Callers should allocate a new GeojsonWriter instance for each ResultStream.</p>
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class GeojsonWriter {
    protected Boolean hasCSVMap = null;
    protected List<ViewPath> paths = null;
    protected String geometryProp;
    
    public GeojsonWriter() {
    }
    
    protected boolean hasCSVMap(TreeResult result) {
        if (hasCSVMap == null) {
            Call call = result.getCall();
            EndpointSpec spec = call.getEndpoint();
            ViewMap viewmap = spec.getView( call.getRequest().getViewName() );
            if ( viewmap.hasCsvMap() ) {
                paths = viewmap.getCsvMap().getPaths();
                hasCSVMap = true;
            } else {
                hasCSVMap = false;
            }
            geometryProp = viewmap.getGeometryProp();
            if (geometryProp == null) {
                throw new WebApiException(Status.NOT_ACCEPTABLE, "GeoJSON format not support for this view");
            }
        }
        return hasCSVMap;
    }
    
    /**
     * Write out a single result as a GeoJSON Feature,
     * assumes caller will write any FeatureCollection wrapper
     */
    public void write(TreeResult result, JSFullWriter out) {
       out.startObject();
       out.pair("type", "Feature");
       out.key("geometry");
       // TODO working here
//       out.print( result.get( ViewPath.fromDotted(geometryProp) ) );
       out.finishObject();
    }
}
