package pt.isel.ps1314v.g11.hama.example;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

import pt.isel.ps1314v.g11.common.graph.Algorithm;
import pt.isel.ps1314v.g11.common.graph.Vertex;

public class ExampleAlgorithm extends
		Algorithm<Text, DoubleWritable, NullWritable> {

	private static final Logger LOG = Logger
			.getLogger(ExampleAlgorithm.class);

	@Override
	public void compute(
			Vertex<Text, DoubleWritable, NullWritable> vertex,
			Iterable<DoubleWritable> messages) {

		LOG.info("Superstep " + getSuperstep() + " on vertex with id "
				+ vertex.getId());

		if (getSuperstep() == 2) {
			/*
			 * Will halt the computation in the second superstep.
			 */
			vertex.voteToHalt();
		}

	}

}
