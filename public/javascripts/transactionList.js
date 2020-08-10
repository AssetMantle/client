let transactionListPageNumber = 1;

function getTransactionList(change = 0) {
    transactionListPageNumber = transactionListPageNumber + change;
    componentResource('transactionListPage', jsRoutes.controllers.ComponentViewController.transactionListPage(transactionListPageNumber), 'transactionListPageChangeSpinner', 'pageChange');
    showHideTransactionListBackButtons();
}

$(document).ready(function () {
    showHideTransactionListBackButtons();
});

function showHideTransactionListBackButtons() {
    if (transactionListPageNumber === 1) {
        hideElement('transactionList_BACK');
    } else {
        showElement('transactionList_BACK');
    }
}