package ca.on.oicr.gps.util

import java.util.LinkedHashMap

/**
 * Implementation of autovivifying multidimensional maps, along the lines of Perl's
 * hash use, very handy for building up a complex data structure without having to
 * set properties to arrays all the time. Obviously, all leaf structures are themselves
 * maps with no values. 
 * 
 * See http://groovy.329449.n5.nabble.com/Multidimensional-maps-td362232.html
 * 
 * @author Stuart Watt
 */

class MultiMap extends LinkedHashMap {
	
	@Override
	public Object get(Object key) {
		if (!containsKey(key)) {
			put(key, new MultiMap())
		}
		return super.get(key)
	}
}
