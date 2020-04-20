function onDocumentListIncomplete(source) {
    $('#negotiationDocumentListCompleted')[0].checked = false;
    submitForm(source);
}

function onDocumentSelect(source) {
    let selctedDocument = source.id;
    const form = $(source).closest("form");
    if ($(source).prop('checked') === false) {
        const form = $(source).closest("form");
        let inputElement = form.find("input[value='" + selctedDocument + "']")[0];
        inputElement.value = '';
    } else {
        $('#negotiationDocumentListCompleted')[0].checked = false;
        let elem = form.find("input[value='']")[0];
        elem.value = selctedDocument;
        submitForm(source);
    }
}

function intializeCheckBox(value) {
    $('#' + value).prop('checked', true);
}