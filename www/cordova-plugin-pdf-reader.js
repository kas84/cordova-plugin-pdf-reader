var argscheck = require('cordova/argscheck'),
               exec = require('cordova/exec');

var pdfreader = {

	openPdf: function(title,url,buttonsArray,successCallback, errorCallback){
		exec(successCallback, errorCallback, "PDFViewer", "openPdf", [title,url,buttonsArray]);
	},

	closePdf: function(successCallback, errorCallback) {
		exec(successCallback, errorCallback, "PDFViewer", "closePdf", []);
	}

};

module.exports = pdfreader;