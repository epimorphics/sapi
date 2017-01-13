/******************************************************************
 * File:        JSONLDSupport.java
 * Created by:  Dave Reynolds
 * Created on:  30 Nov 2012
 *
 * (c) Copyright 2012, Epimorphics Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *****************************************************************/

package com.epimorphics.simpleAPI.jsonld;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.jena.rdf.model.Model;

import com.epimorphics.util.EpiException;
import com.fasterxml.jackson.core.JsonParseException;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

/**
 * Utilities for assisting with JSON-LD parsing and serialization 
 * based on a provided JSONLD context
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class JsonLDUtil {

    public static final String MIME_JSONLD = "application/ld+json";
    public static final String FULL_MIME_JSONLD = "application/ld+json; charset=UTF-8";
    public static final MediaType MT_JSONLD = new MediaType("application", "ld+json");
    public static final String CONTEXT_KEY = "@context";

    public static Object readContextFromFile(String file) throws IOException {
        try ( InputStream in = new FileInputStream(file); ) {
            return JsonUtils.fromInputStream( in );
        }
    }

    public static Object contextFromString(String context) throws JsonParseException, IOException {
        return JsonUtils.fromString(context);
    }
    
    public static Model readModel(String baseURI, InputStream inputStream, Object context) {
        try {
            Object json = JsonUtils.fromInputStream(inputStream);
            inputStream.close();
            return parseModel(baseURI, json, context );
        } catch (Exception e) {
            throw new EpiException(e);
        }
    }

    public static Model parseModel(String baseURI, Object jsonObject, Object context) {
        try {
            // Identify system context
            if (jsonObject instanceof Map<?,?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> top = (Map<String,Object>)jsonObject;
                top.put(CONTEXT_KEY, context);
            }
            JsonLDJenaTripleCallBack callback = new JsonLDJenaTripleCallBack();
            Model m = (Model) JsonLdProcessor.toRDF(jsonObject, callback, new JsonLdOptions(baseURI));
            return m;
        } catch (Exception e) {
            throw new EpiException(e);
        }
    }

    public static Object toJSONLD(Model m, Object context) {
        try {
            Object json = JsonLdProcessor.fromRDF(m, new JenaJSONLDParser());
            json = JsonLdProcessor.compact(json, context, new JsonLdOptions());
            return json;
        } catch (Exception e) {
            throw new EpiException(e);
        }
    }    

}
