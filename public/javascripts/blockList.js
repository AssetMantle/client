blockListPageNumber = 1;

function getBlockList(change = 0) {
    blockListPageNumber += change;
    componentResource('blockListPage', jsRoutes.controllers.ComponentViewController.blockListPage(blockListPageNumber), 'blockListPageChangeSpinner', 'pageChange');
    showHideBlockListBackButtons();
}

$(document).ready(function () {
    showHideBlockListBackButtons();
});

function showHideBlockListBackButtons() {
    if (blockListPageNumber === 1) {
        hideElement('blockList_BACK');
    } else {
        showElement('blockList_BACK');
    }
}