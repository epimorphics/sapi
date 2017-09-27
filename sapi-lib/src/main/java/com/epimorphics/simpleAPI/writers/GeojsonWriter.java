/******************************************************************
 * File:        GeojsonWriter.java
 * Created by:  Dave Reynolds
 * Created on:  16 Jun 2017
 * 
 * (c) Copyright 2017, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.writers;

import java.util.Set;

import javax.ws.rs.core.Response.Status;

import org.apache.jena.rdf.model.RDFNode;

import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.simpleAPI.core.API;
import com.epimorphics.simpleAPI.endpoints.EndpointSpec;
import com.epimorphics.simpleAPI.requests.Call;
import com.epimorphics.simpleAPI.results.TreeResult;
import com.epimorphics.simpleAPI.views.ViewMap;
import com.epimorphics.simpleAPI.views.ViewPath;
import com.epimorphics.util.EpiException;

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
    protected boolean initialized = false;
    protected CSVMap csvmap;
    protected String geometryProp;
    protected ViewPath geometryPath;
    protected JSFullWriter out;
    
    public GeojsonWriter(JSFullWriter out) {
        this.out = out;
    }

    protected void init(TreeResult result) {
        if (!initialized) {
            Call call = result.getCall();
            EndpointSpec spec = call.getEndpoint();
            ViewMap viewmap = spec.getView( call.getRequest().getViewName() );
            if ( viewmap.hasCsvMap() ) {
                csvmap = viewmap.getCsvMap();
            }
            geometryProp = viewmap.getGeometryProp();
            if (geometryProp == null) {
                throw new WebApiException(Status.NOT_ACCEPTABLE, "GeoJSON format not support for this view");
            }
            geometryPath = ViewPath.fromDotted(geometryProp);
            initialized = true;
        }
    }
    
    /**
     * Write out a single result as a GeoJSON Feature,
     * assumes caller will write any FeatureCollection wrapper
     */
    public void write(TreeResult result) {
       init(result);
       out.startObject();
       out.pair("type", "Feature");
       out.key("geometry");
       out.print( getGeometry(result) );
       out.key("properties");
       if ( csvmap != null) {
           writeMappedProperties(result);
       } else {
           TreeResult woGeom = result.cloneWithout(geometryPath);
           JsonWriterUtil.writeResult(woGeom, out);
       }
       out.finishObject();
    }
    
    protected void writeMappedProperties(TreeResult result) {
        API api = result.getCall().getAPI();
        out.startObject();
        for (CSVMap.Entry entry : csvmap.getColumns()) {
            String key = entry.getHeader();
            Set<RDFNode> values = result.get( ViewPath.fromDotted(entry.getPath()) );
            int size = values == null ? 0 : values.size();
            if (size == 0) {
                // skip;
            } else if (size == 1) {
                RDFNode n = values.iterator().next();
                JsonWriterUtil.writeSimpleNode(key, n, out, api, false);
            } else {
                out.startArray();
                for (RDFNode n : values) {
                    JsonWriterUtil.writeSimpleNode(key, n, out, api, true);
                }
                out.finishArray();
            }
        }
        out.finishObject();
    }
    
    protected String getGeometry(TreeResult result) {
        Set<RDFNode> geoms = result.get( geometryPath );
        if (geoms == null) {
            throw new EpiException("No geometry available for geojson rendering");
        }
        if (geoms.size() != 1) {
            throw new EpiException("Failed to find unambiguous geometry in result");
        }
        RDFNode geom = geoms.iterator().next();
        if (geom.isLiteral()) {
            return geom.asLiteral().getLexicalForm();
        } else {
            throw new EpiException("Geometry value must be a literal, resource found");
        }
    }
}
