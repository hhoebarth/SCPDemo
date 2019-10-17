package de.javamagazin.rfcServices.service;

import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;

public interface DocumentService {
	public void setHttpDestionation(Destination httpDestination);
	public byte[] merge(byte[] docx, byte[] xml);
}
