<p># cordova-plugin-pdf-reader</p>
<h2>API Reference</h2>
<h3>PdfReader</h3>
<pre>  PdfReader.openPdf: function(title,url,buttonsArray,successCallback, errorCallback){
		exec(successCallback, errorCallback, "PDFViewer", "openPdf", [title,url,buttonsArray]);
	}
  </pre>
<p><strong>Example:</strong></p>
<pre>  
var onSuccess = function (res) {

            console.info(res);
          
        };

        var onError = function (err) {

            console.error(err);

        };

PdfReader.openPdf("Title Name","iVBORw0KGgoAAAANSUhEUgAAACQAAAAkCAYAAADhAJiYAAAAmUlEQVR42u3X3QqAIAwFYKGg1/auF+rnzbo1hQIZImo6j7HBuf9Q2aYyxiikKAEJSEDh6CcQIAd5S/cG+RhXp83UCxTCLL1OiGKOGphSUDNMCYhi9pqYXFBzTA6IYrYWmFQQGyYFRDGXzep15i/JBlFM7RofBHdlkI+atQcN3RhZ5tgvhivk+gG5oMVQsyz56N8g+bkKSECx3F93twfcz7kPAAAAAElFTkSuQmCC",[{"id":1,"name":"btn1","isDefault":"true"},{"id":2,"name":"btn2","isDefault":"false"}], onSuccess, onError);
 </pre>

