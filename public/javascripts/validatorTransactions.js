let validatorTxsPageNum = 1;

function getValidatorTxs(address, change = 0) {
    validatorTxsPageNum = validatorTxsPageNum + change;
    componentResource('validatorTxs', jsRoutes.controllers.ComponentViewController.validatorTransactionsPerPage(address, validatorTxsPageNum), 'validatorTransactionsPageChangeSpinner', 'pageChange');
    showHideValidatorTxListBackButtons();
}

$(document).ready(function () {
    showHideValidatorTxListBackButtons();
});

function showHideValidatorTxListBackButtons() {
    if (validatorTxsPageNum === 1) {
        hideElement('validatorTransactions_BACK');
    } else {
        showElement('validatorTransactions_NEXT');
        showElement('validatorTransactions_BACK');
    }
}