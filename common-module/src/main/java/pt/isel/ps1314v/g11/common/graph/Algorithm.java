package pt.isel.ps1314v.g11.common.graph;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

public abstract class Algorithm<I extends WritableComparable<?>, V extends Writable, E extends Writable, M extends Writable> {

	private Computation<I, V, E, M> computation;
	public static final String ALGORITHM_CLASS = "pt.isel.ps1314v.g11.common.graph.Computation";

	/**
	 * @param vertex
	 *            - the vertex
	 * @param messages
	 *            - the messages received in the current superstep for the
	 *            vertex.
	 */
	public abstract void compute(Vertex<I, V, E> vertex, Iterable<M> messages);

	// @Override
	public long getSuperstep() {
		return computation.getSuperstep();
	}

	// @Override
	public void sendMessage(I targetVertexId, M message) {
		computation.sendMessageToVertex(targetVertexId, message);
	}

	// @Override
	public void sendMessageToNeighbors(Vertex<I, V, E> vertex, M message) {
		computation.sendMessageToNeighbors(vertex, message);
	}

	// @Override
	public <A extends Writable> void aggregateValue(int index, A value) {
		computation.aggregateValue(index, value);
	}

	// @Override
	public <A extends Writable> A getValueFromAggregator(int index) {
		return computation.getValueFromAggregator(index);
	}

	/**
	 * This method sets the platform computation context therefore should only
	 * be called by the platform.
	 * 
	 * @param computation
	 *            - the platform computation.
	 */
	public void setPlatformComputation(Computation<I, V, E, M> computation) {
		this.computation = computation;
	}
}
