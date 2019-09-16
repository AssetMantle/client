getConfigurationAsynchronously("blockchain.main.wsIP");
getConfigurationAsynchronously("blockchain.main.abciPort");

function transactionExplorer() {
    let wsUrl = getConfiguration("blockchain.main.wsIP") + ":" + getConfiguration("blockchain.main.abciPort") + "/websocket";

    let content = '';
    for (let i = 0; i < 7; i++) {
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
            let receivedData =JSON.parse(evt.data);
            if (receivedData.result.data !== undefined) {
                let height = receivedData.result.data.value.TxResult.height;
                let blockDetails = jsRoutes.controllers.BlockExplorerController.blockDetails(height, height);
                $.ajax({
                    url: blockDetails.url,
                    type: blockDetails.type,
                    async: true,
                    statusCode: {
                        200: function (blockDetailsData) {
                            let blocks = JSON.parse(blockDetailsData);
                            let dataHash = blocks[0].header.data_hash;
                            let transactionContainerList = document.getElementById("transactionContainer");
                            let transactionContainerListLength = transactionContainerList.childNodes.length;
                            if (transactionContainerListLength > 8) {
                                transactionContainerList.removeChild(transactionContainerList.childNodes[transactionContainerListLength - 1]);
                            }
                            $('#transactionContainer').prepend("<tr><td><button onclick='searchFunction(" + JSON.stringify(height) + ")'>" + height + "<td>" + dataHash + "</span></button></td></button></td><td >" + "TODO" + "</td></tr>");
                        }
                    },
                    500: {}
                });
            }
        };
        wsTx.onerror = function (evt) {
            document.getElementById("transactionContainer").appendChild(document.createElement("div").innerHTML = "ERROR: " + evt.data);
        };
    });
}

window.onload = transactionExplorer();