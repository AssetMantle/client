getConfigurationAsynchronously("blockchain.main.wsIP");
getConfigurationAsynchronously("blockchain.main.abciPort");
getConfigurationAsynchronously("blockchain.main.ip");
getConfigurationAsynchronously("blockchain.main.restPort")

function transactionExplorer(){
    let wsUrl = getConfiguration("blockchain.main.wsIP") + ":" + getConfiguration("blockchain.main.abciPort") + "/websocket";
    let txHashUrl = getConfiguration("blockchain.main.ip") + ":" + getConfiguration("blockchain.main.restPort") + "/txs/";
    let blockchainHeightURL = getConfiguration("blockchain.main.ip") + ":" + getConfiguration("blockchain.main.abciPort") + "/block?height=";
    let txHashPage = "./txs?txHash=";

    var content = '';
    for (var i =0; i <8; i++){
        content = "<tr><td></td><td></td><td></td></tr>" + content;
    }
    $('#transaction_container').prepend(content);
    window.addEventListener("load", function (evt) {
        let wsTx = new WebSocket(wsUrl);
        wsTx.onopen = () => {
            let requestTx = `{"method":"subscribe", "id":"dontcare","jsonrpc":"2.0","params":["tm.event='Tx'"]}`;
            wsTx.send(requestTx)
        };

        wsTx.onmessage = function (evt) {
            let dataTx = JSON.parse(evt.data);
            let height = dataTx["result"]["data"]["value"]["TxResult"]["height"];
            let urlHeight = blockchainHeightURL + height;
            let data = JSON.parse(httpGet(urlHeight));
            let txHash = data.result.block_meta.header.data_hash;
            let msgType = JSON.parse(httpGet(txHashUrl + txHash)).tx.value.msg[0].type;

            var transactionContainerList = document.getElementById("transaction_container");
            let transactionContainerListLength = transactionContainerList.childNodes.length;
            if (transactionContainerListLength > 8) {
                transactionContainerList.removeChild(transactionContainerList.childNodes[transactionContainerListLength - 1]);
            }
            $('#transaction_container').prepend("<tr><td><a href='" + blockHeightURL + height + "'>" + height + "</a></td><td><a href='" + txHashPage + txHash + "'>" + txHash + "</a></td><td >" + msgType + "</td></tr>");
        };
        wsTx.onerror = function (evt) {
            document.getElementById("transaction_container").appendChild(document.createElement("div").innerHTML = "ERROR: " + evt.data);
        };
    });
}

window.onload = transactionExplorer();