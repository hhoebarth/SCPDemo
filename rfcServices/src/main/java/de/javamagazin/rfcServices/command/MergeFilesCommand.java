package de.javamagazin.rfcServices.command;

import java.time.Duration;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.exception.DestinationAccessException;
import com.sap.cloud.sdk.cloudplatform.connectivity.exception.DestinationNotFoundException;
import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceConfiguration;
import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceDecorator;
import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceIsolationMode;
import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceRuntimeException;

import de.javamagazin.rfcServices.service.DocumentService;
import de.javamagazin.rfcServices.service.DocumentServiceImpl;

public class MergeFilesCommand {
    private static final Logger logger = LoggerFactory.getLogger(MergeFilesCommand.class);
    
    private final DocumentService documentService;
    private final byte[] docx;
    private final byte[] xml;
    private final ResilienceConfiguration resilienceConfig;
    
    public MergeFilesCommand(byte[] docx, byte[] xml) {
    	this(new DocumentServiceImpl(), docx, xml);
    }
    
    public MergeFilesCommand(DocumentService documentService, byte[] docx, byte[] xml) {
    	this.documentService = documentService;
    	this.docx = docx;
    	this.xml = xml;
    	
    	resilienceConfig = ResilienceConfiguration.of(DocumentService.class)
                .isolationMode(ResilienceIsolationMode.TENANT_AND_USER_OPTIONAL)
                .timeLimiterConfiguration(
                        ResilienceConfiguration.TimeLimiterConfiguration.of()
                                .timeoutDuration(Duration.ofMillis(300000)))
                .bulkheadConfiguration(
                        ResilienceConfiguration.BulkheadConfiguration.of()
                                .maxConcurrentCalls(20));     
    }
    
    public byte[] execute() {
    	return ResilienceDecorator.executeSupplier(this::run, resilienceConfig, e -> {
            logger.warn("Fallback called because of exception.", e);
            return Base64.getDecoder().decode(docx);
        });
    }
    
    private byte[] run() {
    	try {
	        final Destination httpDestination = DestinationAccessor.getDestination("docServices");
	        if(httpDestination != null)
	        {
	        	logger.info("Got the HTTP destination");
	        	documentService.setHttpDestionation(httpDestination);
	        }
    	}
    	catch (DestinationNotFoundException | DestinationAccessException e) {
			logger.error(e.getMessage());
			throw new ResilienceRuntimeException(e);
		}
    	return documentService.merge(docx, xml);
    }
}
