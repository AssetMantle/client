function onDocumentListIncomplete(source) {
    $('#negotiationDocumentListCompleted')[0].checked = false;
    $('#PHYSICAL_DOCUMENTS_HANDLED_VIA').prop('disabled',true);
    submitForm(source);
}

function onDocumentSelect(source) {
    let selectedDocument = source.id;
    const form = $(source).closest("form");
    if ($(source).prop('checked') === false) {
        const form = $(source).closest("form");
        let inputElement = form.find("input[value='" + selectedDocument + "']")[0];
        inputElement.value = '';
    } else {
        $('#negotiationDocumentListCompleted')[0].checked = false;
        if($('#PHYSICAL_DOCUMENTS_HANDLED_VIA').val()==""){
            $('#PHYSICAL_DOCUMENTS_HANDLED_VIA').attr('disabled',true);
        }
        let emptyInputElement = form.find("input[value='']")[0];
        emptyInputElement.value = selectedDocument;
        submitForm(source);
    }
}

function initializeCheckBox(value) {
    $('#' + $.escapeSelector(value)).prop('checked', true);
}