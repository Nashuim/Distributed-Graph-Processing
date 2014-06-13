package pt.isel.ps1314v.g11.llp.hama.io;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hama.graph.Edge;
import org.apache.hama.graph.Vertex;
import org.apache.hama.graph.VertexInputReader;

import pt.isel.ps1314v.g11.llp.LLPVertexValue;

public class LLPVertexInputReader
		extends VertexInputReader<LongWritable, Text, LongWritable, NullWritable, LLPVertexValue> {

	@Override
	public boolean parseVertex(
			LongWritable key,
			Text value,
			Vertex<LongWritable, NullWritable, LLPVertexValue> vertex)
			throws Exception {

		String[] ws = value.toString().split(" ");

		vertex.setVertexID(new LongWritable(Long.parseLong(ws[0])));

		for (int i = 2; i < ws.length; i += 2) {
			vertex.addEdge(new Edge<LongWritable,NullWritable>(
					new LongWritable(Long.parseLong(ws[i])), NullWritable.get()));
		}

		return true;
	}
}
