function verifyReject(source, target, status) {
    $(source).closest("form").find('input[type=checkbox]').prop('checked', status);
    submitForm(source, target);
}