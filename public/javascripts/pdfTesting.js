
/*  document.getElementById("pdfTest").contents().click(function(event){
      console.log("Clicked");
      console.log(event);
    //  console.log(event.);
  });*/
// document.getElementById("pdfTest").contentWindow.document.body.onclick=function()  {
//     console.log("Clicked");
//  console.log(e.clientX,e.clientY,e.pageX);
// console.log(e);
//   }

/*document.getElementById("pdfTest").on('click',function(event) {
    console.log("clicked");
    console.log(event) ;
});*/

/*function myFunc(e){
    console.log(e)
}*/

/*$('#pdfTest').on('load',function(){
    $(this).contents().find("body").on('click', function(event) { alert('test'); });
});*/

/*document.getElementById("pdfTest").ownerDocument.addEventListener('click',function(event){
    console.log("clicked");
    console.log(event.clientX);
});*/
/*$('#pdfTest').on('load',function(){
    iframeclick();
});

function iframeclick() {
    document.getElementById("pdfTest").contentWindow.document.body.onclick = function() {
        console.log("clicked");
        document.getElementById("pdfTest").contentWindow.location.reload();
    }
}*//*
$('#pdfTest').on('load',function(){
    console.log("onLoad");
    let x=document.getElementById("pdfTest");
    console.log("getElement");
    console.log(x);
    let y=document.getElementById("pdfTest").contentWindow;
    console.log("contentWindow");
    console.log(y);
    let z=document.getElementById("pdfTest").contentWindow.document;
    console.log("contentWindow.document");
    console.log(z);
    document.getElementById("pdfTest").contentWindow.document.addEventListener('onclick',function(event){
        console.log("somethinergeg");
        console.log(event.pageX);
        console.log(event.pageY);
        console.log(event);
    });
});
*/

/*   modifyPdf();

   async function modifyPdf() {

 const pdfSrc=document.getElementById("pdfTest").src;
 console.log(pdfSrc);
       const existingPdfBytes = await fetch(pdfSrc).then(res => res.arrayBuffer());
//   const existingPdfBytes = pdf.arrayBuffer();
       console.log(existingPdfBytes);
 const pdfDoc = await PDFLib.PDFDocument.load(existingPdfBytes,{ ignoreEncryption: true });
 const helveticaFont = await pdfDoc.embedFont(PDFLib.StandardFonts.Helvetica);

 const pages = pdfDoc.getPages();
 const firstPage = pages[0];
 const { width, height } = firstPage.getSize();
 firstPage.drawText('This text was added with JavaScript!', {
     x: 5,
     y: height / 2 + 300,
     size: 50,
     font: helveticaFont,
     color: PDFLib.rgb(0.95, 0.1, 0.1),
     rotate: PDFLib.degrees(-45),
 });

// const pdfBytes = await pdfDoc.save();
       console.log("qweqweq");
 const pdfDataUri= await pdfDoc.saveAsBase64({dataUri: true});
 document.getElementById("pdfTest").src=pdfDataUri;
}
*/

WebViewer({
    path: '../../../assets/WebViewer/lib', // path to the PDFTron 'lib' folder on your server
    licenseKey: 'Insert commercial license key here after purchase',
    initialDoc: "/getPDF?name=account.pdf"
// initialDoc: '/path/to/my/file.pdf',  // You can also use documents on your server
}, document.getElementById('viewer'))
.then(instance => {
    const docViewer = instance.docViewer;
    const annotManager = instance.annotManager;
    // call methods from instance, docViewer and annotManager as needed
    // you can also access major namespaces from the instance as follows:
    // const Tools = instance.Tools;
    // const Annotations = instance.Annotations;
   // console.log(instance.contextMenuPopup);
   // instance.disableElements([ 'leftPanel', 'leftPanelButton' ]);
    $("#viewer").append("<input type='file' id='browseUploadButton' name='savePDF' hidden>");
    //document.getElementById('viewer').append("<input type='file' id='browseUploadButton' name='savePDF'>")
    instance.disableReplyForAnnotations(function(annotation) {
        return annotation instanceof instance.Annotations.Annotation;
    });

    docViewer.on('documentLoaded', async() => {
        // call methods relating to the loaded document


    });
    document.getElementById("savePDF").onclick=async function () {
        const doc = docViewer.getDocument();
        //console.log(doc.);
        const xfdfString = await annotManager.exportAnnotations();
        console.log(xfdfString);
        const options = { xfdfString };
        console.log(options);
        const data = await doc.getFileData(options);
        console.log(data);
        const arr = new Uint8Array(data);
        console.log(arr);
        const blob = new Blob([arr], { type: 'application/pdf' });
        console.log(blob);
        console.log(blob.size);
        blob.name="account.pdf";
        const rFile = new Resumable({
            target: "/userUpload/pdf",
            fileType: ['jpg', 'png', 'jpeg', 'pdf', 'doc', 'docx'],
            query: {csrfToken: '1e3f07c9509eea1be292104f7177802d6a6b4e69-1578461072229-c02fabbf2c5c92313fed698d'}
        });
        rFile.on('fileAdded', function(file, event){
            rFile.upload();
        });
        rFile.addFile(blob);
    }
});
/*
jQuery(function ($) {
    if (typeof $.fn.annotator !== 'function') {
        alert("Ooops! it looks like you haven't built the Annotator concatenation file. " +
            "Either download a tagged release from GitHub, or modify the Cakefile to point " +
            "at your copy of the YUI compressor and run `cake package`.");
    } else {
        // This is the important bit: how to create the annotator and add
        // plugins
        $('#testPdf').annotator()
            .annotator('addPlugin', 'Permissions')
            .annotator('addPlugin', 'Markdown')
            .annotator('addPlugin', 'Tags');

        $('#testPdf').data('annotator').plugins['Permissions'].setUser("Joe Bloggs");
    }
});*/
