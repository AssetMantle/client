function replaceDocument(data) {
    const newDocument = document.open("text/html", "replace");
    newDocument.write(data);
    newDocument.close();
    $(window).trigger("replace");
}