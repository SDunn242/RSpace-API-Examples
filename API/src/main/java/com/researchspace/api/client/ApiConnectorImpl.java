package com.researchspace.api.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.researchspace.api.clientmodel.ApiFile;
import com.researchspace.api.clientmodel.Document;
import com.researchspace.api.clientmodel.DocumentSearchResult;
import com.researchspace.api.clientmodel.Field;
import com.researchspace.api.clientmodel.FilePost;
import com.researchspace.api.clientmodel.FileSearchResult;
import com.researchspace.api.clientmodel.LinkItem;

/**
 * Main helper class providing methods for connecting to RSpace API.
 * The connection code uses serverURL and apiKey properties set in config file.  
 */
public class ApiConnectorImpl implements ApiConnector {
    
    private static final String API_DOCUMENTS_ENDPOINT = "/api/v1/documents";
    private static final String API_FILES_ENDPOINT = "/api/v1/files"; 
    
    private static final int SOCKET_TIMEOUT = 15000;
    private static final int CONNECT_TIMEOUT = 15000;

    private final String serverURL;
    private final String apiKey;

    public ApiConnectorImpl() throws IOException {
        ConfigPropertiesReader configReader = new ConfigPropertiesReader();
        serverURL = configReader.getConfigProperty("serverURL");
        apiKey = configReader.getConfigProperty("apiKey");
    }
    
    public ApiConnectorImpl(String serverUrl, String apiKey) {
        this.serverURL = serverUrl;
        this.apiKey = apiKey;
    }

    /* (non-Javadoc)
	 * @see com.researchspace.api.client.ApiConnector#makeDocumentSearchRequest(java.lang.String, java.util.Map)
	 */
    @Override
	public DocumentSearchResult searchDocuments(String searchQuery, Map<String, String> searchParams) 
	        throws URISyntaxException, IOException {
        return makeDocSearchRequest(searchQuery, null, searchParams);
    }

    /* (non-Javadoc)
	 * @see com.researchspace.api.client.ApiConnector#makeDocumentSearchRequest(com.researchspace.api.client.AdvancedQuery, java.util.Map)
	 */
    @Override
	public DocumentSearchResult searchDocuments(AdvancedQuery advQuery, 
            Map<String, String> searchParams) throws URISyntaxException, IOException {

        return makeDocSearchRequest(null, advQuery, searchParams);
    }

    private DocumentSearchResult makeDocSearchRequest(String searchQuery, AdvancedQuery advQuery, 
            Map<String, String> searchParams) throws URISyntaxException, IOException {

        if (searchParams == null) {
            searchParams = new HashMap<String, String>();
        }
        if (searchQuery != null && searchQuery.length() > 0) {
            searchParams.put("query", searchQuery);
        }
        if (advQuery != null) {
            searchParams.put("advancedQuery", advQuery.toJSON());
        }
        
        URIBuilder builder = new URIBuilder(getApiDocumentsUrl());
        for (Entry<String, String> param : searchParams.entrySet()) {
            builder.setParameter(param.getKey(), param.getValue());
        }
        String uri = builder.build().toString();
        String docSearchResponse = makeApiGetRequest(uri).asString();
        ObjectMapper mapper = createObjectMapper();
       
        return mapper.readValue(docSearchResponse, DocumentSearchResult.class);
    };
    
    @Override
    public Document createDocument(Document document) throws IOException {
        String docAsString = makeDocumentApiPostRequest(document).asString();
        ObjectMapper mapper = createObjectMapper();
        return mapper.readValue(docAsString, Document.class);
    }
    
    /* (non-Javadoc)
	 * @see com.researchspace.api.client.ApiConnector#makeSingleDocumentRequest(long)
	 */
    @Override
	public Document retrieveDocument(long docID) throws IOException {
        String docAsString = makeApiGetRequest(getApiSingleDocumentUrl(docID)).asString();
        ObjectMapper mapper = createObjectMapper();
        return mapper.readValue(docAsString, Document.class);
    }

	private ObjectMapper createObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
		mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
		return mapper;
	}

    /* (non-Javadoc)
	 * @see com.researchspace.api.client.ApiConnector#makeSingleCSVDocumentRequest(long)
	 */
    @Override
	public String retrieveDocumentAsCSV(long docID) throws IOException {
        return makeApiGetRequest(getApiSingleDocumentUrl(docID), "text/csv").asString();
    }
    
    /* (non-Javadoc)
	 * @see com.researchspace.api.client.ApiConnector#makeLinkedObjectRequest(java.lang.String, java.lang.Class)
	 */
    @Override
	public <T> T retrieveLinkedObject(String link, Class<T> objectType) throws IOException {
        String objectAsString = makeApiGetRequest(link, "application/json").asString();
        ObjectMapper mapper = createObjectMapper();
        return mapper.readValue(objectAsString, objectType);
    }

    @Override
    public Document updateDocument(Document document) throws IOException {
        prepareDocumentForPutRequest(document);
        String docAsString = makeDocumentApiPutRequest(document).asString();
        ObjectMapper mapper = createObjectMapper();
        return mapper.readValue(docAsString, Document.class);
    }

    // if document was retrieved by API these will be in invalid format (json). 
    // date fields are unmodifiable anyway. 
    private void prepareDocumentForPutRequest(Document document) {
        document.setCreated(null);
        document.setLastModified(null);
        if (document.getFields() != null) {
            for (Field field : document.getFields()) {
                field.setLastModified(null);
            }
        }
    }

    /* (non-Javadoc)
	 * @see com.researchspace.api.client.ApiConnector#makeFileSearchRequest(java.lang.String, java.util.Map)
	 */
    @Override
	public FileSearchResult searchFiles(String mediaType, 
            Map<String, String> searchParams) throws URISyntaxException, IOException {

        if (searchParams == null) {
            searchParams = new HashMap<String, String>();
        }
        if (mediaType != null && mediaType.length() > 0) {
            searchParams.put("mediaType", mediaType);
        }
        
        URIBuilder builder = new URIBuilder(getApiFilesUrl());
        for (Entry<String, String> param : searchParams.entrySet()) {
            builder.setParameter(param.getKey(), param.getValue());
        }
        String uri = builder.build().toString();
        String docSearchResponse = makeApiGetRequest(uri).asString();
        ObjectMapper mapper = createObjectMapper();
        return mapper.readValue(docSearchResponse, FileSearchResult.class);
    }

    @Override
    public ApiFile retrieveFileById(long fileId) throws IOException {
        String docSearchResponse = makeApiGetRequest(getApiSingleFileUrl(fileId)).asString();
        ObjectMapper mapper = createObjectMapper();
        return mapper.readValue(docSearchResponse, ApiFile.class);
    }

    /* (non-Javadoc)
     * @see com.researchspace.api.client.ApiConnector#makeFileDataRequest(com.researchspace.api.client.model.ApiFile)
     */
    @Override
    public InputStream retrieveFileData(ApiFile apiFile) throws IOException {
        String fileDataLink = apiFile.getLinkByType(LinkItem.ENCLOSURE_REL);
        return makeApiGetRequest(fileDataLink).asStream();
    }

    @Override
    public ApiFile uploadFile(FilePost filePost) throws IOException {
        String fileUploadResponse = makeFileUploadApiRequest(filePost).asString();
        ObjectMapper mapper = createObjectMapper();
        return mapper.readValue(fileUploadResponse, ApiFile.class);
    }

    private Content makeFileUploadApiRequest(FilePost filePost) throws IOException {
        HttpEntity fileUploadEntity = MultipartEntityBuilder
                .create()
                .addBinaryBody("file", filePost.getFile())
                .addTextBody("folderId", filePost.getFolderId() == null ? "" : "" + filePost.getFolderId())
                .addTextBody("caption", filePost.getCaption())
                .build();
        Response response = Request.Post(getApiFilesUrl())
                .addHeader("apiKey", apiKey)
                .body(fileUploadEntity)
                .connectTimeout(CONNECT_TIMEOUT)
                .socketTimeout(SOCKET_TIMEOUT)
                .execute();
        return response.returnContent();
    }
    
    protected Content makeApiGetRequest(String uriString) throws IOException {
        return makeApiGetRequest(uriString, "application/json");
    }
    
    private Content makeDocumentApiPostRequest(Document document) throws IOException {
        Validate.notNull(document);
        ObjectMapper mapper = createObjectMapper();
        String documentAsJson = mapper.writeValueAsString(document);
        Response response = Request.Post(getApiDocumentsUrl())
                .addHeader("apiKey", apiKey)
                .bodyString(documentAsJson, ContentType.APPLICATION_JSON)
                .connectTimeout(CONNECT_TIMEOUT)
                .socketTimeout(SOCKET_TIMEOUT)
                .execute();
        return response.returnContent();
    }
    
    private Content makeDocumentApiPutRequest(Document document) throws IOException {
        Validate.notNull(document.getId());
        String docUpdateUrl = getApiSingleDocumentUrl(document.getId());
        System.out.println("updating document url: " + docUpdateUrl);
        ObjectMapper mapper = createObjectMapper();
        String documentAsJson = mapper.writeValueAsString(document);
        System.out.println("updating document with json: " + documentAsJson);
        Response response = Request.Put(docUpdateUrl)
                .addHeader("apiKey", apiKey)
                .bodyString(documentAsJson, ContentType.APPLICATION_JSON)
                .connectTimeout(CONNECT_TIMEOUT)
                .socketTimeout(SOCKET_TIMEOUT)
                .execute();
        return response.returnContent();
    }

    /* makes the HTTP query and returns the results as a Content object */
    protected Content makeApiGetRequest(String uriString, String responseContentType) throws IOException {
        Response response = Request.Get(uriString)
                .addHeader("Accept", responseContentType)
                .addHeader("apiKey", apiKey)
                .connectTimeout(CONNECT_TIMEOUT)
                .socketTimeout(SOCKET_TIMEOUT)
                .execute();
        return response.returnContent();
    }
    
    protected String getApiDocumentsUrl() {
        return serverURL + API_DOCUMENTS_ENDPOINT;
    }

    protected String getApiSingleDocumentUrl(long docId) {
        return getApiDocumentsUrl() + "/" + docId;
    }

    protected String getApiFilesUrl() {
        return serverURL + API_FILES_ENDPOINT;
    }

    protected String getApiSingleFileUrl(long fileId) {
        return getApiFilesUrl() + "/" + fileId;
    }

}
