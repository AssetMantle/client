function onDocumentListIncomplete(source) {
    $('#negotiationDocumentsListCompleted')[0].checked = false;
    submitForm(source);
}