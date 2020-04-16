function verifyReject(source, target, status) {
    $(source).closest("form").find('[type=checkbox]').prop('checked', status);
    submitForm(source, target);
}