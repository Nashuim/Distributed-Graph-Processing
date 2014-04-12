package pt.isel.ps1314v.g11.common.graph;

import org.apache.hadoop.io.Writable;

public interface Aggregator<V extends Writable> {
	
	/**Aggregates the specified value.
	 * @param value - the value to be aggregated by the aggregator.
	 */
	void aggregate(V value);
	
	/**
	 * @return the aggregated value.
	 */
	V getValue();
	
	/**
	 * @return the initial value of an aggregator to be used by some BSP platforms.
	 */
	V initialValue();
}