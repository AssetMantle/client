let showAllBlocksTableClick = 0;

function showAllBlocksInitialTableContent() {
    showAllBlocksTableClick = 0;
    changeTableContent(showAllBlocksTableClick);
}

function onShowAllBlocksClickNext() {
    showAllBlocksTableClick++;
    changeTableContent(showAllBlocksTableClick);
}

function onShowAllBlocksClickPrevious() {
    showAllBlocksTableClick--;
    if (showAllBlocksTableClick > 0) {
        changeTableContent(showAllBlocksTableClick);
    } else {
        showAllBlocksInitialTableContent();
    }
}

function changeTableContent(clickValue) {
    let lastBlockHeightURL = jsRoutes.controllers.BlockExplorerController.lastBlockHeight();
    $.ajax({
        url: lastBlockHeightURL.url,
        type: lastBlockHeightURL.type,
        async: true,
        statusCode: {
            200: function (lastBlockHeight) {
                let blockDetails = jsRoutes.controllers.BlockExplorerController.blockDetails((lastBlockHeight - 10 * (clickValue + 1)), (lastBlockHeight - 10 * clickValue));
                $.ajax({
                    url: blockDetails.url,
                    type: blockDetails.type,
                    async: true,
                    statusCode: {
                        200: function (blockDetailsData) {
                            let blocks = JSON.parse(blockDetailsData);
                            let content = '';
                            Array.prototype.forEach.call(blocks, block => {
                                content = content + "<tr><td>" + block.header.height + "</td><td>" + block.header.time + "</td><td>" + block.header.num_txs + "</td></td></tr>";
                            });
                            $('#allBlocksTableBody').empty().append(content);
                        }
                    },
                });
            }
        }
    });
}

function updateShowAllBlocksTable(receivedData) {
    if (showAllBlocksTableClick === 0) {
        let height = receivedData.result.data.value.block.header.height;
        let time = receivedData.result.data.value.block.header.time;
        let blockContainerList = document.getElementById("allBlocksTableBody");
        if (!((receivedData.result.data) === undefined)) {
            let numberOfTransactions = receivedData.result.data.value.block.header.num_txs;
            blockContainerList.removeChild(blockContainerList.childNodes[(blockContainerList.childNodes.length - 1)]);
            $('#allBlocksTableBody').prepend("<tr><td>" + height + "</td><td>" + time + "</td><td>" + numberOfTransactions + "</td></td></tr>");
        }
    }
}

function showAllBlocksTable() {
    $('#blockHeightBottomDivision').hide();
    $('#txHashBottomDivision').hide();
    $('#indexBottomDivision').hide();
    $('#validatorsTable').hide();
    showAllBlocksInitialTableContent();
    $('#allBlocksTable').show();
}