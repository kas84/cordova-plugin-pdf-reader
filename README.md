# cordova-plugin-pdf-reader

<h2> API Reference </h2>
<h3> PdfReader </h3>

  <pre>
  PdfReader.openPdf: function(title,url,buttonsArray,successCallback, errorCallback){
		exec(successCallback, errorCallback, "PDFViewer", "openPdf", [title,url,buttonsArray]);
	}
  </pre>
