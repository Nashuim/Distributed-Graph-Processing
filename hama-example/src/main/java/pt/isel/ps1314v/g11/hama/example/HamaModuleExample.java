package pt.isel.ps1314v.g11.hama.example;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.bsp.HashPartitioner;
import org.apache.hama.bsp.SequenceFileInputFormat;
import org.apache.hama.bsp.TextOutputFormat;
import org.apache.hama.commons.io.TextArrayWritable;
import org.apache.hama.graph.Edge;
import org.apache.hama.graph.GraphJob;
import org.apache.hama.graph.Vertex;
import org.apache.hama.graph.VertexInputReader;

import pt.isel.ps1314v.g11.common.config.CommonConfig;
import pt.isel.ps1314v.g11.hama.config.HamaModuleConfiguration;

public class HamaModuleExample {
	
	
	/**
	 * Example VertexInputReader.
	 * @see <a href="https://hama.apache.org/hama_graph_tutorial.html">Hama Graph Tutorial</a>
	 */
	public static class PagerankSeqReader
			extends
			VertexInputReader<Text, TextArrayWritable, Text, NullWritable, DoubleWritable> {
		@Override
		public boolean parseVertex(Text key, TextArrayWritable value,
				Vertex<Text, NullWritable, DoubleWritable> vertex)
				throws Exception {
			vertex.setVertexID(key);

			for (Writable v : value.get()) {
				vertex.addEdge(new Edge<Text, NullWritable>((Text) v, null));
			}

			return true;
		}
	}

	public static void main(String... args) throws IOException,
			ClassNotFoundException, InterruptedException {
		
		HamaConfiguration conf =  new HamaConfiguration();
		
		GraphJob job = new GraphJob(conf,
				HamaModuleConfiguration.class);

		/*
		 * Some properties to make it easier to run locally
		 */
		
		conf.set("bsp.master.address", "local");
		conf.set("bsp.local.tasks.maximum", "2");
		conf.set("fs.default.name", "file:///");
		
		job.setVertexInputReaderClass(PagerankSeqReader.class);

		job.setVertexIDClass(Text.class);
		job.setVertexValueClass(DoubleWritable.class);
		job.setEdgeValueClass(NullWritable.class);

		job.setInputFormat(SequenceFileInputFormat.class);

		job.setPartitioner(HashPartitioner.class);
		job.setOutputFormat(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);

		job.setInputPath(new Path("/usr/local/hama/randomgraph"));
		job.setOutputPath(new Path("/home/andre/result"));

		CommonConfig moduleConfig = new CommonConfig(
				new HamaModuleConfiguration(job));

		moduleConfig.setAlgorithmClass(ExampleAlgorithm.class);
		moduleConfig.preparePlatformConfig();

		job.waitForCompletion(true);

	}
}
