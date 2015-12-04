/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.simpleAPI.queryTransforms;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.sparql.query.Transforms;

public class AppTransforms extends ComponentBase {

	final List<String> transformNames = new ArrayList<String>();
	
	public AppTransforms() {		
	}
	
	public void setInclude(String name) {
		transformNames.add(name);
	}
	
	public void fill(Transforms ts) {
		for (String name: transformNames) {
			ts.add(name, Transforms.get(name));
		}
	}

	public void update(Transforms ts, String name) {
		
	}
}
