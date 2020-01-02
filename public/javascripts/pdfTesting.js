
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

function myFunc(e){
    console.log(e)
}

document.getElementById("pdfTest").addEventListener('mousemove',function(event){
    console.log("somethinergeg");
    console.log(event.pageX);
    console.log(event.pageY);
    console.log(event);
});
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