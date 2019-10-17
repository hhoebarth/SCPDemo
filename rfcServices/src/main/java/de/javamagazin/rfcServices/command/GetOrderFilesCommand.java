package de.javamagazin.rfcServices.command;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

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

import de.javamagazin.rfcServices.service.OrderService;
import de.javamagazin.rfcServices.service.OrderServiceImpl;

public class GetOrderFilesCommand {

    private static final Logger logger = LoggerFactory.getLogger(GetOrderFilesCommand.class);
    
    private final OrderService orderService;
    private final String order;
    private final ResilienceConfiguration resilienceConfig;
    
    public GetOrderFilesCommand(String order) {
    	this(new OrderServiceImpl(), order);
    }
    
    public GetOrderFilesCommand(OrderService orderService, String order) {
    	this.orderService = orderService;
    	this.order = order;
    	
    	resilienceConfig = ResilienceConfiguration.of(OrderService.class)
                .isolationMode(ResilienceIsolationMode.TENANT_AND_USER_OPTIONAL)
                .timeLimiterConfiguration(
                        ResilienceConfiguration.TimeLimiterConfiguration.of()
                                .timeoutDuration(Duration.ofMillis(10000)))
                .bulkheadConfiguration(
                        ResilienceConfiguration.BulkheadConfiguration.of()
                                .maxConcurrentCalls(20));   
    	
    	final ResilienceConfiguration.CacheConfiguration cacheConfig =
    	        ResilienceConfiguration.CacheConfiguration
    	                .of(Duration.ofSeconds(10))
    	                .withParameters(order);

    	resilienceConfig.cacheConfiguration(cacheConfig);  
    }
    
    public Map<String, byte[]> execute() {
    	return ResilienceDecorator.executeSupplier(this::run, resilienceConfig, e -> {
            logger.warn("Fallback called because of exception.", e);
            return Collections.emptyMap();
        });
    }
    
    private Map<String, byte[]> run() {
    	try{
	        final Destination rfcDestination = DestinationAccessor.getDestination("de1_rfc");
	        if(rfcDestination != null)
	        {
	        	logger.info("Got the RFC destination");
	        	orderService.setRfcDestionation(rfcDestination);
	        }
    	}
	    catch (DestinationNotFoundException | DestinationAccessException e) {
			logger.error(e.getMessage());
			throw new ResilienceRuntimeException(e);
		}	
        
    	return orderService.getOrderFiles(order);
    }
}
