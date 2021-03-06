package pt.isel.ps1314v.g11.common.aggregator;

import org.apache.hadoop.io.DoubleWritable;

/**
 * Aggregator that aggregates calculating the min between Double values. 
 * Initial value is {@link Double#MAX_VALUE}.
 */
public class DoubleMinAggregator extends BaseAggregator<DoubleWritable>{

	@Override
	public void aggregate(DoubleWritable value) {
		getValue().set(Math.min(value.get(), getValue().get()));
	}

	@Override
	public DoubleWritable initialValue() {
		return new DoubleWritable(Double.MAX_VALUE);
	}

}
