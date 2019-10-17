package de.javamagazin.rfcServices.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceRuntimeException;

public class DocumentServiceImpl implements DocumentService {
	private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);
	
	private Destination httpDestination;
	
	@Override
	public void setHttpDestionation(Destination httpDestination) {
		this.httpDestination = httpDestination;
	}
	
	@Override
	public byte[] merge(byte[] docx, byte[] xml) {
		final HttpResponse mergeResponse;
		
		try {
			mergeResponse = __getHttpClient().execute(__getHttpPost(docx, xml));
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new ResilienceRuntimeException(e);
		}	
		
		logger.info("reponse " + mergeResponse.getStatusLine() + mergeResponse.toString());
		
		int status = mergeResponse.getStatusLine().getStatusCode();
        if (status < 200 || status >= 300) {
        	logger.error("merge service not working");
			throw new ResilienceRuntimeException("merge service not working");
        }
        
		final HttpEntity entity = mergeResponse.getEntity();
		if(entity != null) {
			logger.info("Merge service executed - result received");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				entity.writeTo(baos);
			} catch (IOException e) {
				logger.error(e.getMessage());
				throw new ResilienceRuntimeException(e);
			}
			return baos.toByteArray();
		}
		else
		{
			logger.error("merge service not working");
			throw new ResilienceRuntimeException("merge service not working");
		}
	}
	
	private HttpClient __getHttpClient() {
		final HttpClient httpClient = HttpClientAccessor.getHttpClient(httpDestination.asHttp());
		
		return httpClient;
	}
	
	private HttpPost __getHttpPost(byte[] docx, byte[] xml) {
		HttpPost httpPost = new HttpPost("/v1/docServices/mergeRemote");
		httpPost.addHeader("Content-Type","multipart/mixed; boundary=\"---Content Boundary\"");
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();   
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

		builder.addBinaryBody("docx", docx, ContentType.create("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), "template.docx"); 
		builder.setBoundary("---Content Boundary");
		builder.addBinaryBody("xml", xml, ContentType.APPLICATION_XML, "data.xml"); 
		builder.setBoundary("---Content Boundary");
		httpPost.setEntity(builder.build());
		
		return httpPost;
	}
}
