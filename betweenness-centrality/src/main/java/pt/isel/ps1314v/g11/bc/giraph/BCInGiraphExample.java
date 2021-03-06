package pt.isel.ps1314v.g11.bc.giraph;

import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.utils.InternalVertexRunner;

import pt.isel.ps1314v.g11.bc.BetweennessCentralityAlgorithm;
import pt.isel.ps1314v.g11.bc.giraph.io.AdjacencyListBCInputFormat;
import pt.isel.ps1314v.g11.bc.giraph.io.JsonBCOutputFormat;
import pt.isel.ps1314v.g11.bc.util.BCJobBean;
import pt.isel.ps1314v.g11.common.aggregator.BooleanAndAggregator;
import pt.isel.ps1314v.g11.common.aggregator.DoubleMaxAggregator;
import pt.isel.ps1314v.g11.common.aggregator.DoubleMinAggregator;
import pt.isel.ps1314v.g11.common.config.CommonConfig;
import pt.isel.ps1314v.g11.giraph.config.GiraphModuleConfiguration;
import pt.isel.ps1314v.g11.giraph.util.ExampleFileRunner;

public class BCInGiraphExample {
	public static void main(String[] args) throws Exception {
		GiraphConfiguration conf = new GiraphConfiguration();

		/*
		 * To run on the Local job Runner
		 */
		conf.set("giraph.SplitMasterWorker", "false");

		conf.setVertexInputFormatClass(AdjacencyListBCInputFormat.class);
		conf.setVertexOutputFormatClass(JsonBCOutputFormat.class);
		conf.setWorkerConfiguration(1, 1, 100);
		//conf.set("mapreduce.job.counters.limit", "240");
		
		GiraphModuleConfiguration giraphConfig = new GiraphModuleConfiguration(conf);
		CommonConfig commonConfig = new CommonConfig(giraphConfig);
		
		commonConfig.setAlgorithmClass(BetweennessCentralityAlgorithm.class);
		
		BCJobBean config = BCJobBean.parseArgs(args);
		commonConfig.setStrings(BetweennessCentralityAlgorithm.START_VERTEXES,
				
				config.getStarts()
				
				
				/* new String[]{
				"0",
				"1",
				"2",
				"3",
				"4",
//				"5",
//				"6",
//				"7",
//				"8",
//				"9",
//				"10",
//				"11",
//				"12",
//				"13",
//				"14",
//				"15"
		}*/);
		
		commonConfig.setBoolean(BetweennessCentralityAlgorithm.NORMALIZE, config.shouldNormalize());
		
		//Only needed when BetweennessCentralityAlgorithm.NORMALIZE is set to true.
		if(config.shouldNormalize()){
			commonConfig.registerAggregator(BetweennessCentralityAlgorithm.AGG_ENDED, BooleanAndAggregator.class);
			commonConfig.registerAggregator(BetweennessCentralityAlgorithm.AGG_MIN_BC, DoubleMinAggregator.class);
			commonConfig.registerAggregator(BetweennessCentralityAlgorithm.AGG_MAX_BC, DoubleMaxAggregator.class);
		}
		
		commonConfig.preparePlatformConfig();
		
		String[] graph = new String[] {
				"0 0 1 1",
				"1 0 0 1 2 1",
				"2 0 1 1"
		};

		
		String input = config.getInputPath();
		String output = config.getOutputPath();
		if(input != null && output != null){
			ExampleFileRunner.run(input, output, conf);
		}else{
			Iterable<String> its = InternalVertexRunner.run(conf, graph);
			 if (its != null){
			 	for (String r : its) {
			 		System.out.println(r);
			 	}
			 }
		}
	}
}
