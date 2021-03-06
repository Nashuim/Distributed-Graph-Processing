package pt.isel.ps1314v.g11.hama.graph;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.graph.GraphJobRunner;
import org.apache.log4j.Logger;

import pt.isel.ps1314v.g11.common.graph.Algorithm;
import pt.isel.ps1314v.g11.common.graph.BasicAlgorithm;
import pt.isel.ps1314v.g11.common.graph.Computation;
import pt.isel.ps1314v.g11.common.graph.Edge;
import pt.isel.ps1314v.g11.common.graph.KeyValueWritableDummy;
import pt.isel.ps1314v.g11.common.graph.Vertex;
import pt.isel.ps1314v.g11.hama.util.IteratorsUtil;
import pt.isel.ps1314v.g11.hama.util.IteratorsUtil.KeyCompare;

import com.google.common.collect.Lists;

/**
 * This class maps a {@link org.apache.hama.graph.Vertex} to a {@link Computation} and a {@link Vertex}.
 *
 * @param <I> Vertex Id
 * @param <V> Vertex Value and Message value
 * @param <E> Edge Value
 */
public class HamaComputationMapper<I extends WritableComparable<I>, V extends Writable, E extends Writable, M extends Writable>
		extends org.apache.hama.graph.Vertex<I, E, V> implements
		Computation<I, V, E, M>, Vertex<I, V, E> {

	public static String VERTEX_VALUE_KEY = "pt.isel.ps1314v.g11.hama.vertex_value";
	public static Class<? extends Writable> HAMA_VERTEX_VALUE_CLASS;
	
	private HamaEdgeKeyElemCompare hamaEdgeComparator =  new HamaEdgeKeyElemCompare();
	//private CommonEdgeKeyElemCompare commonEdgeComparator = new CommonEdgeKeyElemCompare();
	Logger LOG = Logger.getLogger(HamaComputationMapper.class);
	//Replicated edges mapping to common edges
	//private List<Edge<I, E>> commonEdges = new ArrayList<>();
	
	private Algorithm<I, V, E,M> algorithm;
	private boolean setupCalled = false;
	private HamaConfiguration conf;

	@SuppressWarnings("unchecked")
	@Override
	public void setup(HamaConfiguration conf) {
		super.setup(conf);
		this.conf = conf;
		algorithm = (Algorithm<I, V, E,M>) ReflectionUtils
				.newInstance(conf.getClass(Algorithm.ALGORITHM_CLASS,
						BasicAlgorithm.class), conf);
	
		algorithm.setPlatformComputation(this);
		
		setupCalled = true;
	}

	@Override
	public long getSuperstep() {
		return super.getSuperstepCount();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void sendMessageToVertex(I targetVertexId, M message) {
		try {
			super.sendMessage(targetVertexId, (V)message);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void sendMessageToNeighbors(Vertex<I, V, E> vertex, M message) {
		for (Edge<I, E> edge : vertex.getVertexEdges())
			sendMessageToVertex(edge.getTargetVertexId(), message);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <A extends Writable> void aggregateValue(String key, A value) {
		try {
			super.aggregate(0, (V) new KeyValueWritableDummy(key, value));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A extends Writable> A getValueFromAggregator(String key) {
		return (A)((MapWritable)super.getAggregatedValue(0)).get(new Text(key));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void compute(Iterable<V> messages) throws IOException {
		if(!setupCalled) setup(getConf());
		if(algorithm == null){
			throw new RuntimeException("Algorithm is not set.");
		}
		ArrayList<M> messagesList = (ArrayList<M>) Lists.newArrayList(messages);
		algorithm.compute(this,messagesList);
	}

	@Override
	public Iterable<Edge<I, E>> getVertexEdges() {
		return new EdgeIterable(super.getEdges());
	}

	@Override
	public int getNumEdges() {
		return super.getEdges().size();
	}

	@Override
	public void removeEdges(I targetVertexId) {
		/*
		 * Removes the edges from the Hama vertex edges list and the replicated common edges.
		 */
		IteratorsUtil.removeKFromIterator(targetVertexId, super.getEdges().iterator(), hamaEdgeComparator);
		//IteratorsUtil.removeKFromIterator(targetVertexId, commonEdges.iterator(), commonEdgeComparator);
	}
	

	@Override
	public void addEdge(Edge<I, E> edge) {
		super.addEdge(new org.apache.hama.graph.Edge<I, E>(edge.getTargetVertexId(), edge.getValue()));
		//commonEdges.add(edge);
	}

	@Override
	public V getVertexValue() {
		return super.getValue();
	}

	@Override
	public void setVertexValue(V value) {
		super.setValue(value);
	}

	@Override
	public I getId() {
		return super.getVertexID();
	}
	
	/*
	 * To compare ids and hama edges to be removed from the edges list.
	 */
	private class HamaEdgeKeyElemCompare implements KeyCompare<I, org.apache.hama.graph.Edge<I, E>>{
		@Override
		public boolean compareKeyToElem(I key, org.apache.hama.graph.Edge<I,E>  elem) {
			return elem.getDestinationVertexID().equals(key);
		}
		
	}

	@Override
	public long getTotalVertices() {
		return super.getNumVertices();
	}

	
	public boolean delayedRead = false;
	private DataInput savedIn;
	@SuppressWarnings("unchecked")
	@Override
	public void readFields(DataInput in) throws IOException {
			if (in.readBoolean()) {
		      if (getVertexID() == null) {
		        setVertexID((I) GraphJobRunner.createVertexIDObject());
		      }
		      getVertexID().readFields(in);
		    }
		    if (in.readBoolean()) {
		      if (getValue() == null) {
		    	  if(HAMA_VERTEX_VALUE_CLASS == null){
		    		  delayedRead = true;
		    		  savedIn = in;
		    		  return;
		    	 }
		        setValue((V) ReflectionUtils.newInstance( //TODO THE ONLY CHANGE
					HAMA_VERTEX_VALUE_CLASS
					, conf)); 
		      }
		      getValue().readFields(in);
		    }
		    readContinue(in);

	}
	
	private void readContinue(DataInput in) throws IOException{

	    setEdges(new ArrayList<org.apache.hama.graph.Edge<I, E>>());
	    if (in.readBoolean()) {
	      int num = in.readInt();
	      if (num > 0) {
	        for (int i = 0; i < num; ++i) {
	          I vertex = GraphJobRunner.createVertexIDObject();
	          vertex.readFields(in);
	          E edgeCost = null;
	          if (in.readBoolean()) {
	            edgeCost = GraphJobRunner.createEdgeCostObject();
	            edgeCost.readFields(in);
	          }
	          org.apache.hama.graph.Edge<I, E> edge = new org.apache.hama.graph.Edge<I, E>(vertex, edgeCost);
	          addEdge(edge);
	        }

	      }
	    }
	    //System.out.println("READ");
	    if(in.readBoolean()){
	    	  // if(getRunner()!=null)
			    	//System.out.println("Vertex "+getId()+" halted "+getValue());
	    	voteToHalt();
	    }/* else {
	    	 // if(getRunner()!=null)
			    	System.out.println("Vertex "+getId()+" did not halt "+getValue());
	    }*/
	    
	 
	    //readState(in);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void setRunner(org.apache.hama.graph.GraphJobRunner<I,E,V> runner) {
		super.setRunner(runner);
		if(delayedRead){
			delayedRead = false;
			HamaConfiguration conf = super.getConf();
			
			HAMA_VERTEX_VALUE_CLASS = (Class<? extends Writable>) conf.getClass(VERTEX_VALUE_KEY, Writable.class);
			setValue((V) ReflectionUtils.newInstance( //TODO THE ONLY CHANGE
					HAMA_VERTEX_VALUE_CLASS
					, conf)); 
			try {
				getValue().readFields(savedIn);
				readContinue(savedIn);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		}
		
	};
	
	@Override
	public void write(DataOutput out) throws IOException {
		if (getVertexID() == null) {
		      out.writeBoolean(false);
		    } else {
		      out.writeBoolean(true);
		      getVertexID().write(out);
		    }

		    if (getValue() == null) {
		      out.writeBoolean(false);
		    } else {
		      out.writeBoolean(true);
		      getValue().write(out);
		    }
		    if (getEdges() == null) {
		      out.writeBoolean(false);
		    } else {
		      out.writeBoolean(true);
		      out.writeInt(getEdges().size());
		      for (org.apache.hama.graph.Edge<I, E> edge : getEdges()) {
		        edge.getDestinationVertexID().write(out);
		        if (edge.getValue() == null) {
		          out.writeBoolean(false);
		        } else {
		          out.writeBoolean(true);
		          edge.getValue().write(out);
		        }
		      }
		    }
		 /*   if(getRunner()!=null)
		    System.out.println("Vertex "+getId()+" on superstep "+getSuperstep()+" halted "+super.isHalted());*/
		    out.writeBoolean(super.isHalted());
		   // writeState(out);
	}
	/*@SuppressWarnings("unchecked")
	@Override
	public void readState(DataInput in) throws IOException {
		if(/*!(getValue().getClass() == HamaModuleConfiguration.HAMA_VERTEX_VALUE_CLASS)*//*in.readBoolean()){
			/*super.setValue((V) ReflectionUtils.newInstance(
					HamaModuleConfiguration.HAMA_VERTEX_VALUE_CLASS
					, conf));
			super.getValue().readFields(in);	
		}
	}
	
	@Override
	public void writeState(DataOutput out) throws IOException {
		if(this.getValue() == null)
			out.writeBoolean(false);
		else{
			out.writeBoolean(true);
			this.getValue().write(out);
		}
			
	}
	*/
	private class EdgeIterable implements Iterable<Edge<I,E>>{
		
		private final Iterable<org.apache.hama.graph.Edge<I, E>> iterable;
		
		public EdgeIterable(Iterable<org.apache.hama.graph.Edge<I, E>> iterable){
			this.iterable = iterable;
		}
		
		@Override
		public Iterator<Edge<I, E>> iterator() {
			return new Iterator<Edge<I,E>>() {
				final Iterator<org.apache.hama.graph.Edge<I, E>> iterator = iterable.iterator();
				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public Edge<I, E> next() {
					org.apache.hama.graph.Edge<I, E> edge = iterator.next();
					return new Edge<I,E>(edge.getDestinationVertexID(),edge.getValue());
				}

				@Override
				public void remove() {
					iterator.remove();
				}
			};
		}
		
	}

	@Override
	public void setEdges(Iterable<Edge<I, E>> edges) {
		ArrayList<org.apache.hama.graph.Edge<I, E>> newEdges = new ArrayList<org.apache.hama.graph.Edge<I, E>>();
		for(Iterator<Edge<I,E>> it = edges.iterator(); it.hasNext();){
			Edge<I, E> edge = it.next();
			newEdges.add(new org.apache.hama.graph.Edge<I, E>(edge.getTargetVertexId(),edge.getValue()));
		}
			
		super.setEdges(newEdges);
		
	}

}
