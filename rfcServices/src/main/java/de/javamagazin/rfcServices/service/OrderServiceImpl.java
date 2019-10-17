package de.javamagazin.rfcServices.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.exception.DestinationAccessException;
import com.sap.cloud.sdk.cloudplatform.connectivity.exception.DestinationNotFoundException;
import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceRuntimeException;
import com.sap.cloud.sdk.s4hana.connectivity.exception.RequestExecutionException;
import com.sap.cloud.sdk.s4hana.connectivity.exception.RequestSerializationException;
import com.sap.cloud.sdk.s4hana.connectivity.rfc.RfmRequest;
import com.sap.cloud.sdk.s4hana.connectivity.rfc.RfmRequestResult;

public class OrderServiceImpl implements OrderService {
	private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
	
	private Destination rfcDestination;
	
    private String xml = null;
    private String docx = null;
	
    @Override
	public void setRfcDestionation(Destination rfcDestination) {
		this.rfcDestination = rfcDestination;
	}
	
	@Override
	public Map<String, byte[]> getOrderFiles(String order) {
		RfmRequestResult rfmResult;
		try {
			rfmResult = __getRfmRequest(order).execute(rfcDestination);
		} catch (RequestSerializationException | DestinationNotFoundException | DestinationAccessException
				| RequestExecutionException e) {
			logger.error(e.getMessage());
			throw new ResilienceRuntimeException(e);
		}
		logger.info("RFC executed");
		logger.info("docx length: " + rfmResult.get("E_DOCX").asString().length());
		
		Map<String, byte[]> result = new HashMap<>();
		result.put("docx", rfmResult.get("E_DOCX").asString().getBytes());
		result.put("xml", rfmResult.get("E_XML").asString().getBytes());
		return result;
	}
	
	private RfmRequest __getRfmRequest(String order) {
        RfmRequest rfmRequest = new RfmRequest("Z_PP_PRPP_GET_FILES", false);
        
        rfmRequest.withExporting("I_AUFNR", "AUFNR", order)
        	   .withExporting("I_SPRAS", "SPRAS", "DE")
        	   .withImporting("E_XML", "STRING", xml)
        	   .withImporting("E_DOCX", "STRING", docx);
        
        logger.info("rfmRequest created");
        
        return rfmRequest;
	}
	


}
