package pt.isel.ps1314v.g11.pagerank.hama;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hama.HamaConfiguration;
import org.apache.hama.bsp.HashPartitioner;
import org.apache.hama.bsp.TextInputFormat;
import org.apache.hama.bsp.TextOutputFormat;
import org.apache.hama.graph.Edge;
import org.apache.hama.graph.GraphJob;
import org.apache.hama.graph.VertexInputReader;

import pt.isel.ps1314v.g11.common.config.CommonConfig;
import pt.isel.ps1314v.g11.hama.config.HamaModuleConfiguration;
import pt.isel.ps1314v.g11.heatkernel.RandomWalkAlgorithm;
import pt.isel.ps1314v.g11.pagerank.PageRankAlgorithm;

public class PageRankHamaExample {

	public static class PageRankVertexInputReader 
				extends VertexInputReader<LongWritable, Text, LongWritable, DoubleWritable, DoubleWritable>{

		@Override
		public boolean parseVertex(
				LongWritable key,
				Text value,
				org.apache.hama.graph.Vertex<LongWritable, DoubleWritable, DoubleWritable> vertex)
				throws Exception {

				String[] ws = value.toString().split(" ");

				vertex.setVertexID(new LongWritable(Long.parseLong(ws[0])));
				
				for (int i = 2; i<ws.length; i+=2) {
					vertex.addEdge(
							new Edge<LongWritable, DoubleWritable>(
									new LongWritable(Long.parseLong(ws[i])),
									new DoubleWritable(Double.parseDouble(ws[i+1]))));
				}

				return true;
		}
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
		HamaConfiguration conf =  new HamaConfiguration();
		
		/*
		 * Some properties to make it easier to run 
		 * and debug locally.
		 * 
		 */
//		conf.set("bsp.master.address", "local");
//		conf.set("bsp.local.tasks.maximum", "2");
//		conf.set("fs.default.name", "file:///");
		
		GraphJob job = new GraphJob(conf, PageRankHamaExample.class);
		job.setJobName("PageRankJob");
	    // Vertex reader
		job.setVertexInputReaderClass(PageRankVertexInputReader.class);
		
		job.setInputFormat(TextInputFormat.class);
		job.setOutputFormat(TextOutputFormat.class);
		
		job.setPartitioner(HashPartitioner.class);
		
		job.setInputPath(new Path(args[0]));
		job.setOutputPath(new Path(args[1]));
		
		CommonConfig moduleConfig = new CommonConfig(
				new HamaModuleConfiguration(job));

		
		moduleConfig.setAlgorithmClass(PageRankAlgorithm.class);
		moduleConfig.setInt(RandomWalkAlgorithm.MAX_SUPERSTEPS_CONF, 2);

		
		moduleConfig.preparePlatformConfig();

		job.waitForCompletion(true);
	}
}