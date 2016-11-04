package eu.wdaqua.lodlaundromat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class LODLaundromat {

	public static final String		LL											= "http://lodlaundromat.org/resource/";
	public static final String		LLM											= "http://lodlaundromat.org/metrics/ontology/";
	public static final String		LLO											= "http://lodlaundromat.org/ontology/";
	public static final String		VOID_EXT									= "http://ldf.fi/void-ext#";

	public static final String		BYTE_COUNT									= LLO + "byteCount";
	public static final String		CHAR_COUNT									= LLO + "charCount";
	public static final String		CONTENT_LENGTH								= LLO + "contentLength";
	public static final String		DOWNLOAD_SIZE								= LLO + "downloadSize";
	public static final String		DUPLICATES									= LLO + "duplicates";
	public static final String		LINE_COUNT									= LLO + "lineCount";
	public static final String		ERROR										= LLO + "error";
	public static final String		NUMBER_OF_WARNINGS							= LLO + "number_of_warnings";
	public static final String		TRIPLES										= LLO + "triples";
	public static final String		UNPACKED_SIZE								= LLO + "unpackedSize";

	public static final String		OUT_DEGREE									= LLM + "outDegree";
	public static final String		IN_DEGREE									= LLM + "inDegree";
	public static final String		DATA_TYPES									= VOID_EXT + "dataTypes";
	public static final String		IRI_LENGTH									= LLM + "IRILength";
	public static final String		OBJ_IRI_LENGTH								= LLM + "objIRILength";
	public static final String		PRED_IRI_LENGTH								= LLM + "predIRILength";
	public static final String		SUB_IRI_LENGTH								= LLM + "subIRILength";
	public static final String		LITERALS									= LLM + "literals";
	public static final String		DISTINCT_BLANK_NODES						= VOID_EXT + "distinctBlankNodes";
	public static final String		LANGUAGES									= LLM + "languages";
	public static final String		IRIS										= LLM + "IRIs";

	protected static final String	LODLAUNDROMAT_ENDPOINT						= "http://sparql.backend.lodlaundromat.org";

	protected static final String	VAR_PREDICATE								= "p";
	protected static final String	VAR_OBJECT									= "o";
	protected static final String	VAR_ERRORS									= "errors";
	protected static final String	VAR_URL										= "url";
	protected static final String	VAR_BLANKS									= "blanks";
	protected static final String	VAR_METRICS									= "metrics";
	protected static final String	VAR_IRIS									= "iris";

	protected static final String	QUERY_IRIS_FROM_RESOURCE					= "SELECT ?iris WHERE {<%s> <http://lodlaundromat.org/metrics/ontology/metrics> ?metrics . ?metrics <http://ldf.fi/void-ext#distinctIRIReferences> ?iris}";
	protected static final String	QUERY_TRIPLES_FROM_RESOURCE					= "SELECT ?o WHERE {<%s> <http://lodlaundromat.org/ontology/triples> ?o}";
	protected static final String	QUERY_NUMBER_OF_ERRORS_FROM_RESOURCE		= "SELECT (COUNT(DISTINCT ?error) AS ?errors) WHERE {<%s> <http://lodlaundromat.org/ontology/error> ?error}";
	protected static final String	QUERY_DISTINCT_BLANK_NODES_FROM_RESOURCE	= "SELECT ?blanks WHERE {<%s> <http://lodlaundromat.org/metrics/ontology/metrics> ?metrics . ?metrics <http://ldf.fi/void-ext#distinctBlankNodes> ?blanks}";
	protected static final String	QUERY_NUMBER_OF_WARNINGS_FROM_RESOURCE		= "SELECT ?o WHERE {<%s> <http://lodlaundromat.org/ontology/number_of_warnings> ?o}";
	protected static final String	QUERY_METADATA_FROM_RESOURCE				= "SELECT DISTINCT ?p ?o WHERE {<%s> ?p ?o}";
	protected static final String	QUERY_METRICS_FROM_RESOURCE					= "SELECT ?metrics ?p ?o ?p2 ?o2 WHERE {<http://lodlaundromat.org/resource/969ffd47bc89744e81ab0265735135e0> <http://lodlaundromat.org/metrics/ontology/metrics> ?metrics . ?metrics ?metric ?value . OPTIONAL {?value ?submetric ?subvalue }}";
	protected static final String	QUERY_URL_FROM_RESOURCE						= "SELECT ?url WHERE {<%s> <" + LLO + "url> ?url}";
	protected static final String	QUERY_URL_FROM_RESOURCE_WITH_ARCHIVE		= "SELECT ?url WHERE {?archive <" + LLO + "containsEntry> <%s> . ?archive <" + LLO + "url> ?url}";

	protected static final String	SEPARATOR									= " ";

	protected static final Logger	logger										= LogManager.getLogger(LODLaundromat.class);

	// JCommander parameters
	@Parameter(names = { "-h", "--help" }, help = true, hidden = true)
	protected boolean				help										= false;
	@Parameter(names = { "-i", "--input" }, description = "Input file. Standard input if not specified.")
	protected String				pInput										= null;
	@Parameter(names = { "-o", "--output" }, description = "Output file. Standard output if not specified")
	protected String				pOutput										= null;
	@Parameter(names = { "-r", "--replace" }, description = "Replace resources by download URL in output of Frank documents command.")
	protected boolean				pReplace									= false;

	protected JCommander			jc;

	public static void main(final String[] args) throws FileNotFoundException {
		final LODLaundromat lodLaundromat = new LODLaundromat(args);
		lodLaundromat.run();
	}

	public LODLaundromat(final String[] args) {
		this.jc = new JCommander(this, args);
	}

	protected void run() throws FileNotFoundException {
		if (this.help) {
			this.jc.usage();
			System.exit(0);
		}

		final InputStream inputStream = this.pInput != null ? new BufferedInputStream(new FileInputStream(this.pInput)) : System.in;
		final OutputStream outputStream = this.pOutput != null ? new BufferedOutputStream(new FileOutputStream(this.pOutput)) : System.out;

		if (this.pReplace) {
			writeFrankResourceReplacements(inputStream, outputStream);
		}
	}

	public static int getTriplesResource(final String resourceUrl) throws HttpException {
		ResultSet queryResult;
		int blanks = 0;
		queryResult = query(String.format(QUERY_TRIPLES_FROM_RESOURCE, resourceUrl));
		if (queryResult.hasNext()) {
			blanks = (int) queryResult.next().getLiteral(VAR_OBJECT).getValue();
		}
		return blanks;
	}

	public static int getNumberOfIRIsFromResource(final String resourceUrl) throws HttpException {
		ResultSet queryResult;
		int blanks = 0;
		queryResult = query(String.format(QUERY_IRIS_FROM_RESOURCE, resourceUrl));
		if (queryResult.hasNext()) {
			blanks = (int) queryResult.next().getLiteral(VAR_IRIS).getValue();
		}
		return blanks;
	}

	public static int getNumberOfErrorsFromResource(final String resourceUrl) throws HttpException {
		ResultSet queryResult;
		int blanks = 0;
		queryResult = query(String.format(QUERY_NUMBER_OF_ERRORS_FROM_RESOURCE, resourceUrl));
		if (queryResult.hasNext()) {
			blanks = (int) queryResult.next().getLiteral(VAR_ERRORS).getValue();
		}
		return blanks;
	}

	public static int getDistinctBlankNodesFromResource(final String resourceUrl) throws HttpException {
		ResultSet queryResult;
		int blanks = 0;
		queryResult = query(String.format(QUERY_DISTINCT_BLANK_NODES_FROM_RESOURCE, resourceUrl));
		if (queryResult.hasNext()) {
			blanks = (int) queryResult.next().getLiteral(VAR_BLANKS).getValue();
		}
		return blanks;
	}

	public static int getNumberObWarningsFromResource(final String resourceUrl) throws HttpException {
		ResultSet queryResult;
		int warnings = 0;
		queryResult = query(String.format(QUERY_NUMBER_OF_WARNINGS_FROM_RESOURCE, resourceUrl));
		if (queryResult.hasNext()) {
			warnings = (int) queryResult.next().getLiteral(VAR_OBJECT).getValue();
		}
		return warnings;
	}

	public static String getURLFromResource(final String resourceUrl) throws HttpException {
		ResultSet queryResult;
		String url = null;
		queryResult = query(String.format(QUERY_URL_FROM_RESOURCE, resourceUrl));
		if (queryResult.hasNext()) {
			url = queryResult.next().getResource(VAR_URL).toString();
		} else {
			queryResult = query(String.format(QUERY_URL_FROM_RESOURCE_WITH_ARCHIVE, resourceUrl));
			if (queryResult.hasNext()) {
				url = queryResult.next().getResource(VAR_URL).toString();
			}
		}
		logger.debug("URL obtained from LODLaundromat endpoint: " + url);
		return url;

	}

	public static HashMap<String, Node> getMetadataFromResourceURL(final String resourceUrl) throws HttpException {
		final HashMap<String, Node> metadata = new HashMap<>();
		final ResultSet queryResult = query(String.format(QUERY_METADATA_FROM_RESOURCE, resourceUrl));
		while (queryResult.hasNext()) {
			final QuerySolution solution = queryResult.next();
			metadata.put(solution.getResource(VAR_PREDICATE).getURI(), solution.get(VAR_OBJECT).asNode());
		}
		return metadata;
	}

	public static void writeFrankResourceReplacements(final InputStream inputStream, final OutputStream outputStream) {
		final PrintWriter printWriter = new PrintWriter(outputStream);
		final Scanner scanner = new Scanner(inputStream);
		scanner.useDelimiter("\n");
		getReplacementIteratorFromFrankDocuments(scanner).forEachRemaining(resource -> {
			logger.debug("Writing line: " + resource);
			printWriter.println(resource);
		});
		printWriter.close();
		scanner.close();
	}

	public static Iterator<String> getReplacementIteratorFromFrankDocuments(final Iterator<String> lines) {
		final HashSet<String> replacements = new HashSet<>();
		lines.forEachRemaining(line -> replacements.add(replaceFrankResourceByURL(line)));
		return replacements.iterator();
	}

	public static String replaceFrankResourceByURL(final String line) {
		logger.debug("Processing line: " + line);
		final String[] fields = line.split(SEPARATOR);
		fields[1] = getURLFromResource(fields[1]);
		return StringUtils.join(fields, SEPARATOR);
	}

	public static void writeURLsFromResources(final InputStream inputStream, final OutputStream outputStream) {
		final PrintWriter printWriter = new PrintWriter(outputStream);
		final Scanner scanner = new Scanner(inputStream);
		scanner.useDelimiter("\n");
		getURLIteratorFromResources(scanner).forEachRemaining(resource -> printWriter.println(resource));
		printWriter.close();
		scanner.close();
	}

	public static Iterator<String> getURLIteratorFromResources(final Iterator<String> resources) {
		final HashSet<String> urls = new HashSet<>();
		resources.forEachRemaining(resource -> urls.add(getURLFromResource(resource)));
		return urls.iterator();
	}

	/*
	 * From here helper methods
	 */

	protected static ResultSet query(final String queryString) throws HttpException {
		final Query query = QueryFactory.create(queryString);
		final QueryExecution qexec = QueryExecutionFactory.sparqlService(LODLAUNDROMAT_ENDPOINT, query);
		final ResultSet results = qexec.execSelect();
		return results;

	}

}
