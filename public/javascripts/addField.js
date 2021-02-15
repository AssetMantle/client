function addField(source, fieldID, target) {
    $('#' + $.escapeSelector(fieldID)).prop('checked', true);
    submitForm(source, target);
}