let accountTxsPageNum = 1;

function getAccountTxs(address, change = 0) {
    accountTxsPageNum = accountTxsPageNum + change;
    componentResource('accountTxs', jsRoutes.controllers.ComponentViewController.accountTransactionsPerPage(address, accountTxsPageNum), 'accountTransactionsPageChangeSpinner', 'pageChange');
    showHideAccountTxListBackButtons();
}

$(document).ready(function () {
    showHideAccountTxListBackButtons();
});

function showHideAccountTxListBackButtons() {
    if (accountTxsPageNum === 1) {
        hideElement('accountTransactions_BACK');
    } else {
        showElement('accountTransactions_NEXT');
        showElement('accountTransactions_BACK');
    }
}