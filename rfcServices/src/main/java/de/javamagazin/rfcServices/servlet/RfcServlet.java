package de.javamagazin.rfcServices.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.javamagazin.rfcServices.command.GetOrderFilesCommand;
import de.javamagazin.rfcServices.command.MergeFilesCommand;

@WebServlet("/v1/rfc/orderReport/*")
@ServletSecurity(@HttpConstraint(rolesAllowed = { "Read" }))
public class RfcServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RfcServlet.class);
    
    @Override
    protected void doGet( final HttpServletRequest request, final HttpServletResponse response)
    {
        logger.info("/v1/rfc/orderReport called");
        
        String pathInfo = request.getPathInfo();
        String[] splits = pathInfo.split("/");
        
        if(splits.length != 2) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
        
        String aufnr = splits[1];
        
        try {
        	Map<String, byte[]> files = new GetOrderFilesCommand(aufnr).execute();
        	if(files.isEmpty())
        		files = _getDefaultFiles();

            final byte[] merged = new MergeFilesCommand(files.get("docx"), files.get("xml")).execute();
        	
	        //response.setContentType(entity.getContentType().getValue());
			response.setHeader("Content-Disposition", "attachment; filename=\"result.docx\"");
			response.setContentLength(merged.length);
			OutputStream os = response.getOutputStream();
			os.write(merged, 0, merged.length);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
	            response.getWriter().write(e.getMessage());
	            e.printStackTrace(response.getWriter());
            }
            catch(Exception ex){
            	logger.error(ex.getMessage());
        		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            
            return;
        }
    }
    
    protected Map<String, byte[]> _getDefaultFiles() {
    	logger.info("return default files");
        Map<String, byte[]> defaultResult = new HashMap<>();
        try {
			defaultResult.put("docx", Base64.getEncoder().encode(IOUtils.toByteArray(new FileInputStream(new File(getServletContext().getRealPath("/WEB-INF/template.docx"))))));
			defaultResult.put("xml", Base64.getEncoder().encode(IOUtils.toByteArray(new FileInputStream(new File(getServletContext().getRealPath("/WEB-INF/templateData.xml"))))));
		} catch (IOException e1) {
			logger.error(e1.getMessage());
			return Collections.emptyMap();
		}
        
        return defaultResult;
    }
}
