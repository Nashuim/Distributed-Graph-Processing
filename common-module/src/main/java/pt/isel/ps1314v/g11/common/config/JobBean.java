package pt.isel.ps1314v.g11.common.config;

import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;

public class JobBean {
	@Option(name = "-v", usage = "Uses verbose or not.", metaVar = "in", required = false, handler = BooleanOptionHandler.class)
	private boolean verbose = true;

	@Option(name = "-i", usage = "Sets the input file.", metaVar = "in", required = true)
	private String inFile;

	@Option(name = "-o", usage = "Sets the output file.", metaVar = "out", required = true)
	private String outFile;

	public String getInputPath() {
		return inFile;
	}

	public String getOutputPath() {
		return outFile;
	}

	public boolean verbose() {
		return verbose;
	}
}
