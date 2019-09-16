getConfigurationAsynchronously("blockchain.main.wsIP");
getConfigurationAsynchronously("blockchain.main.abciPort");

function transactionExplorer() {
    const wsUrl = getConfiguration("blockchain.main.wsIP") + ":" + getConfiguration("blockchain.main.abciPort") + "/websocket";

    let content = '';
    for (let i = 0; i < 7; i++) {
        content = "<tr><td></td><td></td><td></td></tr>" + content;
    }
    $('#transactionContainer').prepend(content);
    window.addEventListener("load", function (evt) {
        const wsTx = new WebSocket(wsUrl);
        let transactionContainerList = document.getElementById("transactionContainer");
        wsTx.onopen = () => {
            let requestTx = `{"method":"subscribe", "id":"dontcare","jsonrpc":"2.0","params":["tm.event='Tx'"]}`;
            wsTx.send(requestTx)
        };

        wsTx.onmessage = function (message) {
            let receivedData =JSON.parse(message.data);
            if (receivedData.result.events !== undefined) {
                Array.prototype.forEach.call(receivedData.result.events['tx.hash'], (txHash, index) => {
                    let height = receivedData.result.events['tx.height'][index];
                    let transactionContainerListLength = transactionContainerList.childNodes.length;
                    if (transactionContainerListLength > 8) {
                        transactionContainerList.removeChild(transactionContainerList.childNodes[transactionContainerListLength - 1]);
                    }
                    $('#transactionContainer').prepend("<tr><td><button onclick='searchFunction(" + JSON.stringify(height) + ")'>" + height + "</button></td><td><button onclick='searchFunction("+ JSON.stringify(txHash) +")'>"+ txHash +"</button></td></button></td></tr>");
                });
            }
        };

        wsTx.onerror = function (message) {
            document.getElementById("transactionContainer").appendChild(document.createElement("div").innerHTML = "ERROR: " + message.data);
        };
    });
}

$(document).ready = transactionExplorer();