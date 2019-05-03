getConfigurationAsynchronously("blockchain.main.wsIP");
getConfigurationAsynchronously("blockchain.main.abciPort");
getConfigurationAsynchronously("blockchain.main.ip");
getConfigurationAsynchronously("blockchain.main.restPort");

function transactionExplorer() {
    let wsUrl = getConfiguration("blockchain.main.wsIP") + ":" + getConfiguration("blockchain.main.abciPort") + "/websocket";
    let txHashUrl = getConfiguration("blockchain.main.ip") + ":" + getConfiguration("blockchain.main.restPort") + "/txs/";
    let blockchainHeightURL = getConfiguration("blockchain.main.ip") + ":" + getConfiguration("blockchain.main.abciPort") + "/block?height=";

    let content = '';
    for (let i = 0; i < 8; i++) {
        content = "<tr><td></td><td></td><td></td></tr>" + content;
    }
    $('#transactionContainer').prepend(content);
    window.addEventListener("load", function (evt) {
        let wsTx = new WebSocket(wsUrl);
        wsTx.onopen = () => {
            let requestTx = `{"method":"subscribe", "id":"dontcare","jsonrpc":"2.0","params":["tm.event='Tx'"]}`;
            wsTx.send(requestTx)
        };

        wsTx.onmessage = function (evt) {
            let height = JSON.parse(evt.data)["result"]["data"]["value"]["TxResult"]["height"];
            $.ajax({
                url: blockchainHeightURL + height,
                type: "GET",
                async: true,
                statusCode: {
                    200: function (txHashData) {
                        let txHash = txHashData.result.block_meta.header.data_hash;
                        $.ajax({
                            url: txHashUrl + txHash,
                            type: "GET",
                            async: true,
                            statusCode: {
                                200: function (msgTypeData) {
                                    let msgType = JSON.parse(msgTypeData);
                                    let transactionContainerList = document.getElementById("transactionContainer");
                                    let transactionContainerListLength = transactionContainerList.childNodes.length;
                                    if (transactionContainerListLength > 8) {
                                        transactionContainerList.removeChild(transactionContainerList.childNodes[transactionContainerListLength - 1]);
                                    }
                                    $('#transactionContainer').prepend("<tr><td><button onclick='searchFunction("+ JSON.stringify(height) +")'>" + height + "</button></td><td><button onclick='searchFunction("+ JSON.stringify(txHash) +")'>" + txHash + "</button></td><td >" + msgType.tx.value.msg[0].type + "</td></tr>");
                                }
                            }
                        });
                    }
                }
            });

        };
        wsTx.onerror = function (evt) {
            document.getElementById("transactionContainer").appendChild(document.createElement("div").innerHTML = "ERROR: " + evt.data);
        };
    });
}

window.onload = transactionExplorer();