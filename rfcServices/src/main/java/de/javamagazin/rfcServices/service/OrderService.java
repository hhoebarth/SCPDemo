package de.javamagazin.rfcServices.service;

import java.util.Map;

import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;

public interface OrderService {

	public void setRfcDestionation(Destination rfcDestination);
	public Map<String, byte[]> getOrderFiles(String order);
}
