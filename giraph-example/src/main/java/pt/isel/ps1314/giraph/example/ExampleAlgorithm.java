package pt.isel.ps1314.giraph.example;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.log4j.Logger;

import pt.isel.ps1314v.g11.common.graph.Algorithm;
import pt.isel.ps1314v.g11.common.graph.Vertex;

public class ExampleAlgorithm extends
		Algorithm<LongWritable, DoubleWritable, FloatWritable> {

	private static final Logger LOG = Logger
			.getLogger(GiraphModuleExample.class);

	@Override
	public void compute(
			Vertex<LongWritable, DoubleWritable, FloatWritable> vertex,
			Iterable<DoubleWritable> messages) {

		LOG.info("Superstep " + getSuperstep() + " on vertex with id "
				+ vertex.getId().get());

		if (getSuperstep() == 2) {
			/*
			 * Will halt the computation in the second superstep.
			 */
			vertex.voteToHalt();
		}

	}

}
