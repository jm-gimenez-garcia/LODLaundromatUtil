package eu.wdaqua.lodlaundromat;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class LODLaundromatConfiguration {

	public static final String	DEFAULT_CONFIG_FOLDER	= "res";
	public static final String	DEFAULT_OUTPUT_FOLDER	= "output";
	final String				PROCESSED_TRIPLES_FILE	= "processedTriples.aux";
	final String				PROCESSED_DATASETS_FILE	= "processedDatasets.aux";
	final int					START_DEFAULT			= 1;
	final int					STEP_DEFAULT			= 1;

	JCommander					jc;

	@Parameter(names = { "-h", "--help" }, help = true, hidden = true)
	boolean						help					= false;

	@Parameter(names = { "-a", "--start" }, description = "Line number to start processing")
	int							start					= this.START_DEFAULT;

	@Parameter(names = { "-e", "--step" }, description = "Number of lines to jump before processing the line")
	int							step					= this.STEP_DEFAULT;

	public LODLaundromatConfiguration(final String[] args) {
		this.jc = new JCommander(this, args);
		if (this.help) {
			this.jc.usage();
			System.exit(1);
		}
	}

	public int getStart() {
		return this.start;
	}

	public int getStep() {
		return this.step;
	}

	public String getProcessedTriplesFile() {
		return DEFAULT_CONFIG_FOLDER + "/" + this.PROCESSED_TRIPLES_FILE;
	}

	public String getProcessedDatasetsFile() {
		return DEFAULT_CONFIG_FOLDER + "/" + this.PROCESSED_DATASETS_FILE;
	}

}
