function onDocumentListIncomplete(source) {
    $('#negotiationDocumentListCompleted')[0].checked = false;
    submitForm(source);
}