/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.simpleAPI.queryTransforms;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.appbase.core.Startup;
import com.epimorphics.sparql.query.Transform;
import com.epimorphics.sparql.query.Transforms;

public class AppTransforms extends ComponentBase implements Startup {

	static final Logger log = LoggerFactory.getLogger( AppTransforms.class );

	public final List<String> transformNames = new ArrayList<String>();
	
	public final Transforms transforms = new Transforms();
	
	public AppTransforms() {		
	}	
	
	public void setIncludAll(List<Transform> things) {
		System.err.println("including transforms " + things);
		for (Transform t: things) transforms.add(t);
	}
	
	public void setInclude(Transform t) {
		log.debug("including transform " + t);
		transforms.add(t);
	}
}
