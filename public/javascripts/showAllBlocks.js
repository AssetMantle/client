var click = 0;

function showALlBlocks(abciIpPort, wsURL, bodyID) {
    changeTableContent(abciIpPort, click, bodyID);

    window.addEventListener("load", function (evt) {
        let wsNewBlock = new WebSocket(wsURL);
        wsNewBlock.onopen = () => {
            let requestNewBlock = `{"method":"subscribe", "id":"dontcare","jsonrpc":"2.0","params":["tm.event='NewBlock'"]}`;
            wsNewBlock.send(requestNewBlock)
        };

        wsNewBlock.onmessage = function (message) {
            if (click === 0) {
                let dataNewBlock = JSON.parse(message.data);
                let blockContainerList = document.getElementById(bodyID);
                let height = parseInt(dataNewBlock["result"]["data"]["value"]["block"]["header"]["height"], 10);
                blockContainerList.removeChild(blockContainerList.childNodes[blockContainerList.childNodes.length - 1]);
                $('#' + bodyID).prepend("<tr><td>" + height + "</td><td>" +  dataNewBlock["result"]["data"]["value"]["block"]["header"]["time"] + "</td><td>" + dataNewBlock["result"]["data"]["value"]["block"]["header"]["num_txs"] + "</td></td></tr>");
            }
        };

        wsNewBlock.onerror = function (evt) {
            if (click === 0) {
                document.getElementById(bodyID).appendChild(document.createElement("div").innerHTML = "ERROR: " + evt.data);
            }
        };
    });
}

function onClickNext(abciIpPort, bodyID) {
    click += 1;
    changeTableContent(abciIpPort, click, bodyID);
}

function onClickPrevious(abciIpPort, bodyID) {
    click -= 1;
    changeTableContent(abciIpPort, click, bodyID)
}

function changeTableContent(abciIpPort, clickValue, bodyID) {
    let lastBlockHeight = parseInt(JSON.parse(httpGet(abciIpPort + "/abci_info"))["result"]["response"]["last_block_height"]);
    let url = abciIpPort + "/blockchain?minHeight=" + (lastBlockHeight - 10 * (clickValue + 1)).toString(10) + "&maxHeight=" + (lastBlockHeight - 10 * clickValue).toString(10);
    let blocksData = JSON.parse(httpGet(url));
    let blocks = blocksData["result"]["block_metas"];

    var content = '';
    Array.prototype.forEach.call(blocks, block => {
        content = content + "<tr><td>" + block["header"]["height"] + "</td><td>" + block["header"]["time"] + "</td><td>" + block["header"]["num_txs"] + "</td></td></tr>";
    });
    $('#' + bodyID).empty().append(content);
}