function verifyReject(source, target, statusFieldID, status) {
    $('#' + statusFieldID).prop('checked', status);
    submitForm(source, target);
}

function hideElement(id){
    $("#" + id).hide();
}