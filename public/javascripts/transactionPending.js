function transactionPending(id,spinnerID) {
    $('#'+$.escapeSelector(id)).children().attr('disabled',true);
    $('#'+$.escapeSelector(spinnerID)).show();
}