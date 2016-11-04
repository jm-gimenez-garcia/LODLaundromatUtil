package eu.wdaqua.lodlaundromat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;

public abstract class LodLaundromatProcessor {

	private LODLaundromatConfiguration	conf;

	private final Date					date	= new Date();

	private long						numTriples;
	private int							numDatasets;
	private HashSet<String>				processedDatasets;

	public LodLaundromatProcessor(final String[] args) {
		newConfiguration(args);
		readPartialProcessing();
	}

	protected LODLaundromatConfiguration newConfiguration(final String[] args) {
		return this.conf = new LODLaundromatConfiguration(args);
	}

	protected LODLaundromatConfiguration getConfiguration() {
		return this.conf;
	}

	protected void readPartialProcessing() {
		BufferedReader reader = null;
		String line;
		try {
			try {
				// Read number of triples
				reader = new BufferedReader(new FileReader(getConfiguration().getProcessedTriplesFile()));
				this.numTriples = Long.parseLong(reader.readLine());
				reader.close();
			} catch (final NumberFormatException e) {
				this.numTriples = 0;
				this.numDatasets = 0;
				System.err.println("Error reading number of triples processed. Starting count again.");
			}
			// Read already processed datasets
			this.processedDatasets = new HashSet<String>();
			reader = new BufferedReader(new FileReader(getConfiguration().getProcessedDatasetsFile()));
			while ((line = reader.readLine()) != null) {
				this.processedDatasets.add(line);
				this.numDatasets++;
			}
			System.out.println("Partial status retrieved. Datasets processed: " + this.numDatasets + ". Triples processed: " + this.numTriples);
			System.out.println("Resuming process...");
			reader.close();
		} catch (final FileNotFoundException e) {
			System.err.println("No files to retrieve partial processing status. Starting again.");
			this.processedDatasets = new HashSet<>();
			this.numTriples = 0;
			this.numDatasets = 0;
		} catch (final IOException e) {
			e.printStackTrace();
			System.err.println("Error while retrieven partial processing status. Starting again.");
			this.processedDatasets = new HashSet<>();
			this.numTriples = 0;
			this.numDatasets = 0;
		}
	}

	protected void ProcessInput() {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		BufferedWriter processedDatasetsWriter;
		String line;
		String[] fields = null;
		int numLine = 0;
		if (this.processedDatasets == null) {
			this.processedDatasets = new HashSet<>();
		}

		try {
			processedDatasetsWriter = new BufferedWriter(new FileWriter(getConfiguration().getProcessedDatasetsFile(), true));

			// Flush the partial processing data in case of unexpected stop
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					FileWriter numTriplesWriter = null;
					try {
						numTriplesWriter = new FileWriter(getConfiguration().getProcessedTriplesFile());
						numTriplesWriter.write(Long.toString(LodLaundromatProcessor.this.numTriples) + "\n");
						numTriplesWriter.close();
						processedDatasetsWriter.close();
					} catch (final IOException e) {
						System.err.println("Exception while flushing the partial processing data");
						e.printStackTrace();
					}
				}
			});

			while ((line = reader.readLine()) != null) {
				if (((++numLine) - getConfiguration().getStart()) % getConfiguration().getStep() == 0) {
					fields = line.split(" ");
					if (!this.processedDatasets.contains(fields[1])) {
						processDocument(fields[0], fields[1]);
						processedDatasetsWriter.write(fields[1]);
						processedDatasetsWriter.newLine();
					} else {
						System.out.println(new Timestamp(this.date.getTime()) + fields[1] + " already processed. Skipping.");
					}
				}
			}

		} catch (final IOException e) {
			System.err.println(new Timestamp(this.date.getTime()) + " IOException while processing dataset " + fields[1]);
			e.printStackTrace();
			System.err.println(new Timestamp(this.date.getTime()) + " Resuming the process...");
		}
	}

	protected abstract void processDocument(String downloadUrl, String resourceUrl);

}
