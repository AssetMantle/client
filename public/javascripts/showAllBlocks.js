getConfigurationAsynchronously("blockchain.main.wsIP");
getConfigurationAsynchronously("blockchain.main.abciPort");
getConfigurationAsynchronously("blockchain.main.ip");

setCookie("showAllBlocksTableClick", 0, 1);

function initialTableContent() {
    let wsURL = getConfiguration("blockchain.main.wsIP") + ":" + getConfiguration("blockchain.main.abciPort") + "/websocket";
    setCookie("showAllBlocksTableClick", 0, 1);
    changeTableContent(parseInt(getCookie("showAllBlocksTableClick"), 10));

    window.addEventListener("load", function (evt) {
        let wsNewBlock = new WebSocket(wsURL);
        wsNewBlock.onopen = () => {
            let requestNewBlock = `{"method":"subscribe", "id":"dontcare","jsonrpc":"2.0","params":["tm.event='NewBlock'"]}`;
            wsNewBlock.send(requestNewBlock)
        };

        wsNewBlock.onmessage = function (message) {
            if (parseInt(getCookie("showAllBlocksTableClick"), 10) === 0) {
                let dataNewBlock = JSON.parse(message.data);
                let blockContainerList = document.getElementById("allBlocksTableBody");
                let height = parseInt(dataNewBlock["result"]["data"]["value"]["block"]["header"]["height"], 10);
                blockContainerList.removeChild(blockContainerList.childNodes[(blockContainerList.childNodes.length - 1)]);
                $('#allBlocksTableBody').prepend("<tr><td>" + height + "</td><td>" + dataNewBlock["result"]["data"]["value"]["block"]["header"]["time"] + "</td><td>" + dataNewBlock["result"]["data"]["value"]["block"]["header"]["num_txs"] + "</td></td></tr>");
            }
        };

        wsNewBlock.onerror = function (evt) {
            if (parseInt(getCookie("showAllBlocksTableClick"), 10) === 0) {
                document.getElementById("allBlocksTableBody").appendChild(document.createElement("div").innerHTML = "ERROR: " + evt.data);
            }
        };
    });
}

function onClickNext() {
    let click = parseInt(getCookie("showAllBlocksTableClick"), 10) + 1;
    setCookie("showAllBlocksTableClick", click, 1);
    changeTableContent(click);
}

function onClickPrevious() {
    let click = parseInt(getCookie("showAllBlocksTableClick"), 10) - 1;
    setCookie("showAllBlocksTableClick", click, 1);
    if (click > 0) {
        changeTableContent(click);
    } else {
        initialTableContent();
    }
}

function changeTableContent(clickValue) {
    let abciIpPort = getConfiguration("blockchain.main.ip") + ":" + getConfiguration("blockchain.main.abciPort");
    $.ajax({
        url: abciIpPort + "/abci_info",
        type: "GET",
        async: true,
        statusCode: {
            200: function (lastBlockHeightData) {
                let lastBlockHeight = lastBlockHeightData.result.response.last_block_height;
                $.ajax({
                    url: abciIpPort + "/blockchain?minHeight=" + (lastBlockHeight - 10 * (clickValue + 1)).toString(10) + "&maxHeight=" + (lastBlockHeight - 10 * clickValue).toString(10),
                    type: "GET",
                    async: true,
                    statusCode: {
                        200: function (blocksData) {
                            let blocks = blocksData.result.block_metas;
                            let content = '';
                            Array.prototype.forEach.call(blocks, block => {
                                content = content + "<tr><td>" + block["header"]["height"] + "</td><td>" + block["header"]["time"] + "</td><td>" + block["header"]["num_txs"] + "</td></td></tr>";
                            });
                            $('#allBlocksTableBody').empty().append(content);
                        }
                    }
                });
            }
        }
    });
}

function showAllBlocksTable() {
    $('#blockHeightBottomDivision').hide();
    $('#txHashBottomDivision').hide();
    $('#indexBottomDivision').hide();
    $('#validatorsTable').hide();
    initialTableContent();
    $('#allBlocksTable').show();
}