package de.javamagazin.docServices.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

import de.javamagazin.docServices.service.DocService;

@RestController
public class DocController {

    private static final Logger logger = LoggerFactory.getLogger(DocController.class);
    
    @Autowired
    private DocService docService;

    @PostMapping(value = "/v1/docServices/merge", produces="application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    @HystrixCommand(fallbackMethod = "defaultMerge", commandProperties = {
    		@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "30000")
    })
    public ResponseEntity<byte[]> merge(@RequestParam(value = "docx", required = false) MultipartFile docx,
    									@RequestParam(value = "xml", required = false) MultipartFile xml) throws Docx4JException, IOException, InterruptedException 
    {
        logger.info("v1/docServices/merge called");
        return __merge(docx, xml);
    }

    @PostMapping(value = "/v1/docServices/mergeRemote", produces="application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    public ResponseEntity<byte[]> mergeRemote(@RequestParam(value = "docx", required = false) MultipartFile docx,
    									@RequestParam(value = "xml", required = false) MultipartFile xml) throws Docx4JException, IOException, InterruptedException 
    {
        logger.info("v1/docServices/mergeRemote called");
        return __merge(docx, xml);
    }
    
    private ResponseEntity<byte[]> __merge(MultipartFile docx, MultipartFile xml) throws Docx4JException, IOException, InterruptedException 
	{
		logger.info("v1/docServices/__merge called");
		byte[] docxBytes = docx.getBytes();
		byte[] xmlBytes= xml.getBytes();
		
		InputStream docxStream = null;
		InputStream xmlStream = null;
		
		try {
			docxStream = org.apache.commons.codec.binary.Base64.isBase64(docxBytes)?
					new ByteArrayInputStream(Base64.getDecoder().decode(docxBytes)):new ByteArrayInputStream(docxBytes);
		}
		catch (IllegalArgumentException e) {
			docxStream = new ByteArrayInputStream(docxBytes);
		}
		
		try {	
			xmlStream = org.apache.commons.codec.binary.Base64.isBase64(xmlBytes)?
					new ByteArrayInputStream(Base64.getDecoder().decode(xmlBytes)):new ByteArrayInputStream(xmlBytes);
		}
		catch (IllegalArgumentException e) {
			xmlStream = new ByteArrayInputStream(docxBytes);
		}
		
		return ResponseEntity.ok(((ByteArrayOutputStream) docService.merge(docxStream, xmlStream)).toByteArray());
	}    
    
    public ResponseEntity<byte[]> defaultMerge(MultipartFile docx, MultipartFile xml)
    {
    	logger.info("fallback method called");
    	try {
			return ResponseEntity.ok(docx.getBytes());
		} catch (IOException e) {
			logger.error(e.getMessage());
			return null;
		}
    }
}
