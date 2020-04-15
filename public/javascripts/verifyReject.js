function verifyReject(source, target, unusedInput, status) {
    $(source).closest("form").find('[type=checkbox]').prop('checked', status);
    submitForm(source, target);
}

function verifyRejectCommonForm(source, target, status) {
    $(source).closest("form").find('[type=checkbox]').prop('checked', status);
    console.log(target, status)
    submitForm(source, target);
}