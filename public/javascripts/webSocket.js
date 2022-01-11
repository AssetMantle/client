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
    switch (receivedData.messageType) {
        case 'NEW_BLOCK':
            onNewBlock(receivedData.messageValue);
            break;
        case 'BLOCKCHAIN_CONNECTION_LOST':
            onBlockchainConnectionLost(receivedData.messageValue);
            break;
        case 'PRIVATE_MESSAGE':
            onPrivateMessage(receivedData.messageValue);
            break;
        default :
            console.log("Unknown Message Type");
            break;

    }
}

function onNewBlock(message) {
    $('#blockchainConnectionLost').hide();
    updateLatestBlockHeightCard(message);
    updateBlockList(message);
    updateTransactionList(message);
    updateMissedBlocksCounter(message);
}

function onBlockchainConnectionLost(message) {
    $('#blockchainConnectionLost').fadeIn(100);
}

function onPrivateMessage(message) {
    switch (message.subject) {
        case 'CHAT':
            onChat(message.messageContent);
            break;
        case 'ASSET':
            onAsset(message.messageContent);
            break;
        default :
            console.log("Unknown Private Message Subject Type");
            break;
    }
}

function onChat(message) {
    if ($('#chatMessages').length && $('#' + message.chatID).length) {
        refreshCard('chatMessages', jsRoutes.controllers.ChatController.loadMoreChats(message.chatID, 0), 'CHAT_LOADING');
    }
}

function onAsset(message) {
    console.log(message);
}

function updateLatestBlockHeightCard(message) {
    $('#latestBlockHeight_height').text(message.block.height);
    $('#latestBlockHeight_proposer').text(message.block.proposer);
    convertUTCDateToLocalDate(new Date(message.block.time.toString().replace(/at|Z/g, '')).toString(), "latestBlockHeight_time")
    $('#latestBlockHeight_averageBlockTime').text(message.averageBlockTime + "s");
}

function updateBlockList(message) {
    let blockListPage = $('#blockListPage');
    if (blockListPage.length !== 0 && blockListPageNumber === 1) {
        for (let i = blockListPage.children().length - 2; i >= 0; i--) {
            if (i === 0) {
                $('#blockListPageItemHeight_' + i).text(message.block.height);
                convertUTCDateToLocalDate(new Date(message.block.time.toString().replace(/at|Z/g, '')), `blockListPageItemTime_${i}`)
                $('#blockListPageItemNumTxs_' + i).text(message.txs.length);
                $('#blockListPageItemProposer_' + i).text(message.block.proposer);
            } else {
                $('#blockListPageItemHeight_' + i).text($('#blockListPageItemHeight_' + (i - 1)).text());//.replace(/^\s+|\s+$/g, ''));
                convertUTCDateToLocalDate(new Date($('#blockListPageItemTime_' + (i-1)).text().toString()), `blockListPageItemTime_${i}`)
                $('#blockListPageItemNumTxs_' + i).text($('#blockListPageItemNumTxs_' + (i - 1)).text());
                $('#blockListPageItemProposer_' + i).text($('#blockListPageItemProposer_' + (i - 1)).text());
            }
        }
    }
}

function updateTransactionList(message) {
    let transactionListPage = $('#transactionListPage');
    if (transactionListPage.length !== 0 && transactionListPageNumber === 1) {
        let numTxs = message.txs.length;
        if (numTxs > 0) {
            for (let i = transactionListPage.children().length - 1; i >= 0; i--) {
                if (numTxs >= transactionListPage.children().length) {
                    $('#transactionListPageItemTxHash_' + i).text(message.txs[numTxs - i].hash);
                    $('#transactionListPageItemTxMessageTypes_' + i).text(message.txs[numTxs - i].messageTypes);
                    $('#transactionListPageItemTxHeight_' + i).text(message.block.height);
                    $('#transactionListPageItemTxStatusStatic_' + i).hide();
                    if (message.txs[numTxs - i].status) {
                        $('#transactionListPageItemTxStatusTrue_' + i).show();
                        $('#transactionListPageItemTxStatusFalse_' + i).hide();
                    } else {
                        $('#transactionListPageItemTxStatusTrue_' + i).hide();
                        $('#transactionListPageItemTxStatusFalse_' + i).show();
                    }
                    convertUTCDateToLocalDate(new Date(message.txs[numTxs - i].proposer.toString().replace(/at|Z/g, '')).toString(), `transactionListPageItemTxTime_${i}`)
                } else {
                    if (i <= (numTxs - 1)) {
                        $('#transactionListPageItemTxHash_' + i).text(message.txs[i].hash);
                        $('#transactionListPageItemTxMessageTypes_' + i).text(message.txs[i].messageTypes);
                        $('#transactionListPageItemTxHeight_' + i).text(message.block.height);
                        $('#transactionListPageItemTxStatusStatic_' + i).hide();
                        if (message.txs[i].status) {
                            $('#transactionListPageItemTxStatusTrue_' + i).show();
                            $('#transactionListPageItemTxStatusFalse_' + i).hide();
                        } else {
                            $('#transactionListPageItemTxStatusTrue_' + i).hide();
                            $('#transactionListPageItemTxStatusFalse_' + i).show();
                        }
                        convertUTCDateToLocalDate(new Date(message.txs[numTxs - i].proposer.toString().replace(/at|Z/g, '')).toString(), `transactionListPageItemTxTime_${i}`)
                    } else {
                        $('#transactionListPageItemTxHash_' + i).text($('#transactionListPageItemTxHash_' + (i - 1)).text());
                        $('#transactionListPageItemTxMessageTypes_' + i).text($('#transactionListPageItemTxMessageTypes_' + (i - 1)).text());
                        $('#transactionListPageItemTxHeight_' + i).text($('#transactionListPageItemTxHeight_' + (i - 1)).text());
                        $('#transactionListPageItemTxStatus_' + i).html($('#transactionListPageItemTxStatus_' + (i - 1)).html());
                        convertUTCDateToLocalDate(new Date($('#transactionListPageItemTxTime_' + (i - 1)).text().toString().replace(/at|Z/g, '')).toString(), `transactionListPageItemTxTime_${i}`)
                    }
                }
            }
        }
    }
}

function updateMissedBlocksCounter(message) {
    let blockId = $("#missingBlocksCounter").parent().attr("id");
    let missingBlocksBox = $('#missingBlocksCounter');
    let count = 0;
    for (let i = 0; i < message.validators.length; i++) {
        let missingBlocksCounter = message.validators[i];
        if (blockId === missingBlocksCounter) {
            count++;
        }
    }

    if (count === 0) {
        for (let i = missingBlocksBox.children().length - 1; i >= 0; i--) {
            let previousBlockHeight = $('#missingBlocksCounter_' + (i - 1)).children('.block').attr("id");
            if (i === 0) {
                $('#missingBlocksCounter_' + i).children('.block').html("<div class=\"empty\"></div>");
                $('#missingBlocksCounter_' + i).children('.block').attr("cmuk-tooltip", message.block.height);
                $('#missingBlocksCounter_' + i).children('.block').attr("id", message.block.height);
            } else {
                $('#missingBlocksCounter_' + i).children('.block').html($('#missingBlocksCounter_' + (i - 1)).children('.block').children('div'));
                $('#missingBlocksCounter_' + i).children('.block').attr("id", previousBlockHeight);
                $('#missingBlocksCounter_' + i).children('.block').attr("cmuk-tooltip", previousBlockHeight);
            }
        }
    } else {
        for (let i = missingBlocksBox.children().length - 1; i >= 0; i--) {
            let previousBlockHeight = $('#missingBlocksCounter_' + (i - 1)).children('.block').attr("id");
            if (i === 0) {
                $('#missingBlocksCounter_' + i).children('.block').html("<div class=\"full\"></div>");
                $('#missingBlocksCounter_' + i).children('.block').attr("cmuk-tooltip", message.block.height);
                $('#missingBlocksCounter_' + i).children('.block').attr("id", message.block.height);
            } else {
                $('#missingBlocksCounter_' + i).children('.block').html($('#missingBlocksCounter_' + (i - 1)).children('.block').children('div'));
                $('#missingBlocksCounter_' + i).children('.block').attr("id", previousBlockHeight);
                $('#missingBlocksCounter_' + i).children('.block').attr("cmuk-tooltip", previousBlockHeight);
            }
        }
    }
}

function refreshCard(source, route, loadingSpinnerID = 'commonSpinner') {
    const div = $('#' + source);
    let loadingSpinner = $('#' + loadingSpinnerID);
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        global: showSpinner('refreshCard'),
        beforeSend: function () {
            loadingSpinner.show();
        },
        complete: function () {
            loadingSpinner.hide();
        },
        statusCode: {
            200: function (data) {
                div.html(data)
            },
            401: function (data) {
                div.html(data)
            },
            500: function (data) {
                div.html(data)
            }
        }
    });
}

window.addEventListener("load", init, false);