sap.ui.define([
	"de/javamagazin/rfcServices/controller/BaseController",
	"sap/ui/model/json/JSONModel",
	"sap/m/MessageToast"
], function(BaseController, JSONModel, MessageToast) {
	"use strict";

	return BaseController.extend("de.javamagazin.rfcServices.controller.Home", {
		onInit:function()
		{	
			var data = {orderNumber: ""};
			var oModel = new JSONModel(data);
			this.getView().setModel(oModel);
		},
		
		onAfterRendering: function() 
		{
			 
		},
		
		onBeforeRendering: function() 
		{
		
		},
		
		onSend: function()
		{
			this._download('/v1/rfc/orderReport/' + this.getView().getModel().getProperty("/orderNumber"));
		},
		
		_download: function(url) {
		    var req = new XMLHttpRequest();
		    req.open("GET", url, true);
		    req.responseType = "blob";
		    req.onload = function (event) {
		    	var blob = req.response;
		    	var fileName = "";
		    	var disposition = req.getResponseHeader('Content-Disposition');
			    if (disposition && disposition.indexOf('attachment') !== -1) {
			        var filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
			        var matches = filenameRegex.exec(disposition);
			        if (matches != null && matches[1]) { 
			        	fileName = matches[1].replace(/['"]/g, '');
			        }
			    }
			    if(fileName === "")
			    	MessageToast.show("Report could not be generated");
			    else
			    {
					// to emulate click action
				    // because we cannot save directly to client's computer due to security constraints
				    var a = document.createElement("a");
				    document.body.appendChild(a);
				    a.style = "display: none";
				    a.href = window.URL.createObjectURL(blob);
				    a.download = fileName;
				    a.click();
		
				    document.body.removeChild(a);
			    }
		    }
		    req.send();
	    	MessageToast.show("Download started. This may take some time...");
		}
	});

});