getConfigurationAsynchronously("blockchain.main.wsIP");
getConfigurationAsynchronously("blockchain.main.abciPort");
getConfigurationAsynchronously("blockchain.main.ip");

let showAllBlocksTableBody = "allBlocksTableBody";
let click = 0;

function initialTableContent() {
    let wsURL = getConfiguration("blockchain.main.wsIP") + ":" + getConfiguration("blockchain.main.abciPort") + "/websocket";
    click = 0;
    changeTableContent(click);

    window.addEventListener("load", function (evt) {
        let wsNewBlock = new WebSocket(wsURL);
        wsNewBlock.onopen = () => {
            let requestNewBlock = `{"method":"subscribe", "id":"dontcare","jsonrpc":"2.0","params":["tm.event='NewBlock'"]}`;
            wsNewBlock.send(requestNewBlock)
        };

        wsNewBlock.onmessage = function (message) {
            if (click === 0) {
                let dataNewBlock = JSON.parse(message.data);
                let blockContainerList = document.getElementById(showAllBlocksTableBody);
                let height = parseInt(dataNewBlock["result"]["data"]["value"]["block"]["header"]["height"], 10);
                blockContainerList.removeChild(blockContainerList.childNodes[blockContainerList.childNodes.length - 1]);
                $('#' + showAllBlocksTableBody).prepend("<tr><td>" + height + "</td><td>" + dataNewBlock["result"]["data"]["value"]["block"]["header"]["time"] + "</td><td>" + dataNewBlock["result"]["data"]["value"]["block"]["header"]["num_txs"] + "</td></td></tr>");
            }
        };

        wsNewBlock.onerror = function (evt) {
            if (click === 0) {
                document.getElementById(showAllBlocksTableBody).appendChild(document.createElement("div").innerHTML = "ERROR: " + evt.data);
            }
        };
    });
}

function onClickNext() {
    click += 1;
    changeTableContent(click);
}

function onClickPrevious() {
    click -= 1;
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
                            $('#' + showAllBlocksTableBody).empty().append(content);
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
    $('#allBlocksTable').show();
}

function goBackAllBlocksTable() {
    $('#blockHeightBottomDivision').hide();
    $('#validatorsTable').hide();
    $('#allBlocksTable').hide();
    $('#txHashBottomDivision').hide();
    $('#indexBottomDivision').show();
}