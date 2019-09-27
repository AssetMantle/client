getConfigurationAsynchronously("blockchain.main.wsIP");
getConfigurationAsynchronously("blockchain.main.abciPort");

function transactionExplorer() {
    const wsUrl = getConfiguration("blockchain.main.wsIP") + ":" + getConfiguration("blockchain.main.abciPort") + "/websocket";

    let content = '';
    for (let i = 0; i < 7; i++) {
        content = "<tr><td></td><td></td><td></td></tr>" + content;
    }
    $('#transactionContainer').prepend(content);
    let transactionContainerList = document.getElementById("transactionContainer");

    let wsNewTransaction = new WebSocket(wsUrl);
    wsNewTransaction.onopen = () => {
        let requestTx = `{"method":"subscribe", "id":"dontcare","jsonrpc":"2.0","params":["tm.event='Tx'"]}`;
        wsNewTransaction.send(requestTx)
    };

    wsNewTransaction.onmessage = function (message) {
        if ($('#indexBottomDivision').length === 0) {
            wsNewTransaction.close();
        } else {
            let receivedData = JSON.parse(message.data);
            if (receivedData.result.events !== undefined) {
                Array.prototype.forEach.call(receivedData.result.events['tx.hash'], (txHash, index) => {
                    let height = receivedData.result.events['tx.height'][index];
                    let transactionContainerListLength = transactionContainerList.childNodes.length;
                    if (transactionContainerListLength > 8) {
                        transactionContainerList.removeChild(transactionContainerList.childNodes[transactionContainerListLength - 1]);
                    }
                    $('#transactionContainer').prepend("<tr><td><button onclick='searchFunction(" + JSON.stringify(height) + ")'>" + height + "</button></td><td><button onclick='searchFunction(" + JSON.stringify(txHash) + ")'>" + txHash + "</button></td></button></td></tr>");
                });
            }
        }
    };

    wsNewTransaction.onerror = function (message) {
    };

    wsNewTransaction.onclose = function (event) {
    };

    $(window).replaceAll(function () {
        wsNewTransaction.close();
    });

}

$('#indexBottomDivision').ready(function () {
    transactionExplorer();
});