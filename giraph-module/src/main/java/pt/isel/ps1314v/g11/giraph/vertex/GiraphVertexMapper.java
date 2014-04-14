package pt.isel.ps1314v.g11.giraph.vertex;

import java.util.ArrayList;

import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

import pt.isel.ps1314v.g11.common.graph.Edge;
import pt.isel.ps1314v.g11.giraph.combiner.GiraphEdgeMapper;

public class GiraphVertexMapper<I extends WritableComparable<I>,E extends Writable,M extends Writable> 
						implements pt.isel.ps1314v.g11.common.graph.Vertex<I, E, M>{

	private Vertex<I, M, E> vertex;
	
	public GiraphVertexMapper(Vertex<I, M, E> vertex){
		this.vertex = vertex;
	}
	
	@Override
	public void addEdge(Edge<I, E> edge) {
		vertex.addEdge(new GiraphEdgeMapper<I,E>(edge));
	}
	
	@Override
	public Iterable<Edge<I, E>> getVertexEdges() {
		ArrayList<Edge<I,E>> list = new ArrayList<Edge<I,E>>();
		
		for(org.apache.giraph.edge.Edge<I, E> edge: vertex.getEdges()){
			list.add((GiraphEdgeMapper<I, E>)edge);
		}
		
		return list;
	}

	@Override
	public void setVertexValue(M value) {
		vertex.setValue(value);
	}

	@Override
	public M getVertexValue() {
		return vertex.getValue();
	}

	@Override
	public int getNumEdges() {
		return vertex.getNumEdges();
	}

	@Override
	public void removeEdges(I targetVertexId) {
		vertex.removeEdges(targetVertexId);
	}

	@Override
	public I getId() {
		return vertex.getId();
	}

}
