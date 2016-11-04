package eu.wdaqua.lodlaundromat;

import java.net.MalformedURLException;
import java.util.HashMap;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class LODLaundromat {

	public static final String	LL											= "http://lodlaundromat.org/resource/";
	public static final String	LLM											= "http://lodlaundromat.org/metrics/ontology/";
	public static final String	LLO											= "http://lodlaundromat.org/ontology/";
	public static final String	VOID_EXT									= "http://ldf.fi/void-ext#";

	public static final String	BYTE_COUNT									= LLO + "byteCount";
	public static final String	CHAR_COUNT									= LLO + "charCount";
	public static final String	CONTENT_LENGTH								= LLO + "contentLength";
	public static final String	DOWNLOAD_SIZE								= LLO + "downloadSize";
	public static final String	DUPLICATES									= LLO + "duplicates";
	public static final String	LINE_COUNT									= LLO + "lineCount";
	public static final String	ERROR										= LLO + "error";
	public static final String	NUMBER_OF_WARNINGS							= LLO + "number_of_warnings";
	public static final String	TRIPLES										= LLO + "triples";
	public static final String	UNPACKED_SIZE								= LLO + "unpackedSize";

	public static final String	OUT_DEGREE									= LLM + "outDegree";
	public static final String	IN_DEGREE									= LLM + "inDegree";
	public static final String	DATA_TYPES									= VOID_EXT + "dataTypes";
	public static final String	IRI_LENGTH									= LLM + "IRILength";
	public static final String	OBJ_IRI_LENGTH								= LLM + "objIRILength";
	public static final String	PRED_IRI_LENGTH								= LLM + "predIRILength";
	public static final String	SUB_IRI_LENGTH								= LLM + "subIRILength";
	public static final String	LITERALS									= LLM + "literals";
	public static final String	DISTINCT_BLANK_NODES						= VOID_EXT + "distinctBlankNodes";
	public static final String	LANGUAGES									= LLM + "languages";
	public static final String	IRIS										= LLM + "IRIs";

	static final String			LODLAUNDROMAT_ENDPOINT						= "http://sparql.backend.lodlaundromat.org";

	static final String			VAR_PREDICATE								= "p";
	static final String			VAR_OBJECT									= "o";
	static final String			VAR_ERRORS									= "errors";
	static final String			VAR_URL										= "url";
	static final String			VAR_BLANKS									= "blanks";
	static final String			VAR_METRICS									= "metrics";
	static final String			VAR_IRIS									= "iris";

	static final String			QUERY_IRIS_FROM_RESOURCE					= "SELECT ?iris WHERE {<%s> <http://lodlaundromat.org/metrics/ontology/metrics> ?metrics . ?metrics <http://ldf.fi/void-ext#distinctIRIReferences> ?iris}";
	static final String			QUERY_TRIPLES_FROM_RESOURCE					= "SELECT ?o WHERE {<%s> <http://lodlaundromat.org/ontology/triples> ?o}";
	static final String			QUERY_NUMBER_OF_ERRORS_FROM_RESOURCE		= "SELECT (COUNT(DISTINCT ?error) AS ?errors) WHERE {<%s> <http://lodlaundromat.org/ontology/error> ?error}";
	static final String			QUERY_DISTINCT_BLANK_NODES_FROM_RESOURCE	= "SELECT ?blanks WHERE {<%s> <http://lodlaundromat.org/metrics/ontology/metrics> ?metrics . ?metrics <http://ldf.fi/void-ext#distinctBlankNodes> ?blanks}";
	static final String			QUERY_NUMBER_OF_WARNINGS_FROM_RESOURCE		= "SELECT ?o WHERE {<%s> <http://lodlaundromat.org/ontology/number_of_warnings> ?o}";
	static final String			QUERY_METADATA_FROM_RESOURCE				= "SELECT DISTINCT ?p ?o WHERE {<%s> ?p ?o}";
	static final String			QUERY_METRICS_FROM_RESOURCE					= "SELECT ?metrics ?p ?o ?p2 ?o2 WHERE {<http://lodlaundromat.org/resource/969ffd47bc89744e81ab0265735135e0> <http://lodlaundromat.org/metrics/ontology/metrics> ?metrics . ?metrics ?metric ?value . OPTIONAL {?value ?submetric ?subvalue }}";
	static final String			QUERY_URL_FROM_RESOURCE						= "SELECT ?url WHERE {<%s> <" + LLO + "url> ?url}";
	static final String			QUERY_URL_FROM_RESOURCE_WITH_ARCHIVE		= "SELECT ?url WHERE {?archive <" + LLO + "containsEntry> <%s> . ?archive <" + LLO + "url> ?url}";

	public static int getTriplesResource(final String resourceUrl) throws MalformedURLException, HttpException {
		ResultSet queryResult;
		int blanks = 0;
		queryResult = query(String.format(QUERY_TRIPLES_FROM_RESOURCE, resourceUrl));
		if (queryResult.hasNext()) {
			blanks = (int) queryResult.next().getLiteral(VAR_OBJECT).getValue();
		}
		return blanks;
	}

	public static int getNumberOfIRIsFromResource(final String resourceUrl) throws MalformedURLException, HttpException {
		ResultSet queryResult;
		int blanks = 0;
		queryResult = query(String.format(QUERY_IRIS_FROM_RESOURCE, resourceUrl));
		if (queryResult.hasNext()) {
			blanks = (int) queryResult.next().getLiteral(VAR_IRIS).getValue();
		}
		return blanks;
	}

	public static int getNumberOfErrorsFromResource(final String resourceUrl) throws MalformedURLException, HttpException {
		ResultSet queryResult;
		int blanks = 0;
		queryResult = query(String.format(QUERY_NUMBER_OF_ERRORS_FROM_RESOURCE, resourceUrl));
		if (queryResult.hasNext()) {
			blanks = (int) queryResult.next().getLiteral(VAR_ERRORS).getValue();
		}
		return blanks;
	}

	public static int getDistinctBlankNodesFromResource(final String resourceUrl) throws MalformedURLException, HttpException {
		ResultSet queryResult;
		int blanks = 0;
		queryResult = query(String.format(QUERY_DISTINCT_BLANK_NODES_FROM_RESOURCE, resourceUrl));
		if (queryResult.hasNext()) {
			blanks = (int) queryResult.next().getLiteral(VAR_BLANKS).getValue();
		}
		return blanks;
	}

	public static int getNumberObWarningsFromResource(final String resourceUrl) throws MalformedURLException, HttpException {
		ResultSet queryResult;
		int warnings = 0;
		queryResult = query(String.format(QUERY_NUMBER_OF_WARNINGS_FROM_RESOURCE, resourceUrl));
		if (queryResult.hasNext()) {
			warnings = (int) queryResult.next().getLiteral(VAR_OBJECT).getValue();
		}
		return warnings;
	}

	public static String getURLFromResource(final String resourceUrl) throws MalformedURLException, HttpException {
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
		return url;

	}

	public static HashMap<String, Node> getMetadataFromResourceURL(final String resourceUrl) throws MalformedURLException, HttpException {
		final HashMap<String, Node> metadata = new HashMap<String, Node>();
		final ResultSet queryResult = query(String.format(QUERY_METADATA_FROM_RESOURCE, resourceUrl));
		while (queryResult.hasNext()) {
			final QuerySolution solution = queryResult.next();
			metadata.put(solution.getResource(VAR_PREDICATE).getURI(), solution.get(VAR_OBJECT).asNode());
		}
		return metadata;
	}

	static ResultSet query(final String queryString) throws MalformedURLException, HttpException {
		final Query query = QueryFactory.create(queryString);
		final QueryExecution qexec = QueryExecutionFactory.sparqlService(LODLAUNDROMAT_ENDPOINT, query);
		final ResultSet results = qexec.execSelect();
		return results;

	}

}
