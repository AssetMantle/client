function addField(source, fieldID, target) {
    $('#' + fieldID).prop('checked', true);
    submitForm(source, target);
}