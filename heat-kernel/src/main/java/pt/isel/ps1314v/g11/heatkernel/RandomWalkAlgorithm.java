package pt.isel.ps1314v.g11.heatkernel;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;

import pt.isel.ps1314v.g11.common.graph.BasicAlgorithm;
import pt.isel.ps1314v.g11.common.graph.Vertex;

/**
 * This algorithm is an implementation of a random walk. An algorithm based on a
 * random walk should extends this class.
 *
 */
public abstract class RandomWalkAlgorithm extends
		BasicAlgorithm<LongWritable, DoubleWritable, DoubleWritable> implements
		Configurable {

	private static final String INITIAL_PROBABILITY_CONF = "pt.isel.ps1314v.g11.heatkernel.RandomWalkAlgorithm.jumpFactor";
	private static final String JUMP_FACTOR_CONF = "pt.isel.ps1314v.g11.heatkernel.RandomWalkAlgorithm.initialProbability";

	private static final float DEFAULT_JUMP_FACTOR = 0.85f;

	private DoubleWritable writable = new DoubleWritable();

	private Configuration conf;

	private float jumpFactor;
	private double initialProbability;

	@Override
	public void setConf(Configuration conf) {
		this.conf = conf;

		jumpFactor = conf.getFloat(JUMP_FACTOR_CONF, DEFAULT_JUMP_FACTOR);
		
		initialProbability = conf.getFloat(INITIAL_PROBABILITY_CONF,
				Float.NaN);

		initialProbability = initialProbability == Float.NaN ? getNormalInitialProbability()
				: initialProbability;
	}

	@Override
	public Configuration getConf() {
		return conf;
	}

	public float getJumpFactor() {
		return jumpFactor;
	}

	public double getNormalInitialProbability() {
		return 1d / getTotalVertices();
	}

	public double getInitialProbability() {
		return initialProbability;
	}

	public double stateProbability(
			Vertex<LongWritable, DoubleWritable, DoubleWritable> v) {
		return v.getVertexValue().get() / v.getNumEdges();
	}

	@Override
	public void compute(
			Vertex<LongWritable, DoubleWritable, DoubleWritable> vertex,
			Iterable<DoubleWritable> messages) {
		
		if (getSuperstep() == 0) {
			writable.set(getInitialProbability());
		} else {
			writable.set(recompute(vertex, messages));
		}

		vertex.setVertexValue(writable);

		writable.set(stateProbability(vertex));
		sendMessageToNeighbors(vertex, writable);

		//Will stop at the max iteration defined by the user in the used platform.
	}

	/**
	 * 
	 * @param vertex - the vertex to calculate in this iteration.
	 * @param messages - the messages sent to the vertex in the previous superstep.
	 * @return the new value for the vertex.
	 */
	public abstract double recompute(
			Vertex<LongWritable, DoubleWritable, DoubleWritable> vertex,
			Iterable<DoubleWritable> messages);

}