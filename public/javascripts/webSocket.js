let webSocket;

function init() {
    webSocket = new WebSocket(ws.url);
    webSocket.onopen = onOpen;
    webSocket.onclose = onClose;
    webSocket.onmessage = onMessage;
    webSocket.onerror = onError;
}

function onOpen(event) {
    webSocket.send(ws.start);
}

function onClose(event) {
    init();
}

function onError(event) {
    webSocket.close();
}

function onMessage(event) {
    let receivedData = JSON.parse(event.data);
    if (receivedData.blockchainConnectionLost) {
        $('#blockchainConnectionLost').fadeIn(100);
    } else {
        $('#blockchainConnectionLost').hide();
        updateLatestBlockHeightCard(receivedData);
        updateBlockList(receivedData);
        updateTransactionList(receivedData);
        updateMissedBlocksCounter(receivedData);
    }
}

function updateLatestBlockHeightCard(receivedData) {
    $('#latestBlockHeight_height').text(receivedData.block.height);
    $('#latestBlockHeight_proposer').text(receivedData.block.proposer);
    $('#latestBlockHeight_time').text(receivedData.block.time);
    $('#latestBlockHeight_averageBlockTime').text(receivedData.averageBlockTime + "s");
}

function updateBlockList(receivedData) {
    let blockListPage = $('#blockListPage');
    if (blockListPage.length !== 0 && blockListPageNumber === 1) {
        for (let i = blockListPage.children().length - 1; i >= 0; i--) {
            if (i === 0) {
                $('#blockListPageItemHeight_' + i).text(receivedData.block.height);
                $('#blockListPageItemTime_' + i).text(receivedData.block.time);
                $('#blockListPageItemNumTxs_' + i).text(receivedData.txs.length);
                $('#blockListPageItemProposer_' + i).text(receivedData.block.proposer);
            } else {
                $('#blockListPageItemHeight_' + i).text($('#blockListPageItemHeight_' + (i - 1)).text());//.replace(/^\s+|\s+$/g, ''));
                $('#blockListPageItemTime_' + i).text($('#blockListPageItemTime_' + (i - 1)).text());
                $('#blockListPageItemNumTxs_' + i).text($('#blockListPageItemNumTxs_' + (i - 1)).text());
                $('#blockListPageItemProposer_' + i).text($('#blockListPageItemProposer_' + (i - 1)).text());
            }
        }
    }
}

function updateTransactionList(receivedData) {
    let transactionListPage = $('#transactionListPage');
    if (transactionListPage.length !== 0 && transactionListPageNumber === 1) {
        let numTxs = receivedData.txs.length;
        if (numTxs > 0) {
            for (let i = transactionListPage.children().length - 1; i >= 0; i--) {
                if (numTxs >= transactionListPage.children().length) {
                    $('#transactionListPageItemTxHash_' + i).text(receivedData.txs[numTxs - i].hash);
                    $('#transactionListPageItemTxHeight_' + i).text(receivedData.block.height);
                    $('#transactionListPageItemTxStatusStatic_' + i).hide();
                    if (receivedData.txs[numTxs - i].status) {
                        $('#transactionListPageItemTxStatusTrue_' + i).show();
                        $('#transactionListPageItemTxStatusFalse_' + i).hide();
                    } else {
                        $('#transactionListPageItemTxStatusTrue_' + i).hide();
                        $('#transactionListPageItemTxStatusFalse_' + i).show();
                    }
                    $('#transactionListPageItemTxFees_' + i).text(receivedData.txs[numTxs - i].proposer);
                } else {
                    if (i <= (numTxs - 1)) {
                        $('#transactionListPageItemTxHash_' + i).text(receivedData.txs[i].hash);
                        $('#transactionListPageItemTxHeight_' + i).text(receivedData.block.height);
                        $('#transactionListPageItemTxStatusStatic_' + i).hide();
                        if (receivedData.txs[i].status) {
                            $('#transactionListPageItemTxStatusTrue_' + i).show();
                            $('#transactionListPageItemTxStatusFalse_' + i).hide();
                        } else {
                            $('#transactionListPageItemTxStatusTrue_' + i).hide();
                            $('#transactionListPageItemTxStatusFalse_' + i).show();
                        }
                        $('#transactionListPageItemTxFees_' + i).text(receivedData.txs[i].proposer);
                    } else {
                        $('#transactionListPageItemTxHash_' + i).text($('#transactionListPageItemTxHash_' + (i - 1)).text());
                        $('#transactionListPageItemTxHeight_' + i).text($('#transactionListPageItemTxHeight_' + (i - 1)).text());
                        $('#transactionListPageItemTxStatus_' + i).html($('#transactionListPageItemTxStatus_' + (i - 1)).html());
                        $('#transactionListPageItemTxFees_' + i).text($('#transactionListPageItemTxFees_' + (i - 1)).text());
                    }
                }
            }
        }
    }
}

function updateMissedBlocksCounter(receivedData) {
    let blockId = $("#missingBlocksCounter").parent().attr("id");
    let missingBlocksBox = $('#missingBlocksCounter');
    let count = 0;
    for (let i = 0; i < receivedData.validators.length; i++) {
        let missingBlocksCounter = receivedData.validators[i];
        if (blockId === missingBlocksCounter) {
            count ++;
        }
    }

    if(count === 0){
        for (let i = missingBlocksBox.children().length - 1; i >= 0; i--) {
            let previousBlockHeight = $('#missingBlocksCounter_' + (i - 1)).children('.block').attr("id");
            if (i === 0) {
                $('#missingBlocksCounter_' + i).children('.block').html("<div class=\"empty\"></div>");
                $('#missingBlocksCounter_' + i).children('.block').attr("cmuk-tooltip", receivedData.block.height);
                $('#missingBlocksCounter_' + i).children('.block').attr("id", receivedData.block.height);
            } else {
                $('#missingBlocksCounter_' + i).children('.block').html($('#missingBlocksCounter_' + (i - 1)).children('.block').children('div'));
                $('#missingBlocksCounter_' + i).children('.block').attr("id", previousBlockHeight);
                $('#missingBlocksCounter_' + i).children('.block').attr("cmuk-tooltip", previousBlockHeight);
            }
        }
    }else
    {
        for (let i = missingBlocksBox.children().length - 1; i >= 0; i--) {
            let previousBlockHeight = $('#missingBlocksCounter_' + (i - 1)).children('.block').attr("id");
            if (i === 0) {
                $('#missingBlocksCounter_' + i).children('.block').html("<div class=\"full\"></div>");
                $('#missingBlocksCounter_' + i).children('.block').attr("cmuk-tooltip", receivedData.block.height);
                $('#missingBlocksCounter_' + i).children('.block').attr("id", receivedData.block.height);
            } else {
                $('#missingBlocksCounter_' + i).children('.block').html($('#missingBlocksCounter_' + (i - 1)).children('.block').children('div'));
                $('#missingBlocksCounter_' + i).children('.block').attr("id", previousBlockHeight);
                $('#missingBlocksCounter_' + i).children('.block').attr("cmuk-tooltip", previousBlockHeight);
            }
        }
    }
}

window.addEventListener("load", init, false);