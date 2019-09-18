getConfigurationAsynchronously("blockchain.main.wsIP");
getConfigurationAsynchronously("blockchain.main.abciPort");

function blockExplorer() {
    const blockExplorerTableBody = "blockContainer";
    const wsURL = getConfiguration("blockchain.main.wsIP") + ":" + getConfiguration("blockchain.main.abciPort") + "/websocket";

    setFirstBlockTime();

    const maxNumberOfItems = 8;

    window.addEventListener("load", function (evt) {
        const wsNewBlock = new WebSocket(wsURL);
        wsNewBlock.onopen = () => {
            initializeBlockExplorer(blockExplorerTableBody, maxNumberOfItems);
            showAllBlocksInitialTableContent();
            let requestNewBlock = `{"method":"subscribe", "id":"dontcare","jsonrpc":"2.0","params":["tm.event='NewBlock'"]}`;
            wsNewBlock.send(requestNewBlock)
        };

        wsNewBlock.onmessage = function (message) {
            let receivedData = JSON.parse(message.data);
            if (receivedData.result.data !== undefined) {
                updateBlockExplorer(blockExplorerTableBody, receivedData, maxNumberOfItems);
                updateShowAllBlocksTable(receivedData);
            }
        };

        wsNewBlock.onerror = function (event) {
            blockExplorerErrorEvent(event);
            showAllBlocksTableErrorEvent(event);
        };
    });

}


function updateBlockExplorer(blockExplorerTableBody, receivedData, maxNumberOfItems) {

    let lastBlockTime = getCookie("lastBlockTime");
    let averageBlockTime = parseFloat(getCookie("averageBlockTime"));
    let latestBlockHeight = parseInt(receivedData.result.data.value.block.header.height, 10);
    let numTxs = receivedData.result.data.value.block.header.num_txs;
    let time = receivedData.result.data.value.block.header.time;
    let timerID = "timer" + latestBlockHeight.toString(10);
    let blockTime = new Date(time);
    let differenceBetweenBlockTime = (blockTime.getTime() - new Date(lastBlockTime).getTime()) / 1000;
    let blockContainerList = document.getElementById(blockExplorerTableBody);
    blockContainerList.removeChild(blockContainerList.childNodes[blockContainerList.childNodes.length - 2]);
    $('#' + blockExplorerTableBody).prepend("<tr><td><button onclick='searchFunction(" + JSON.stringify(latestBlockHeight) + ")'>" + latestBlockHeight + "</button></td><td>" + numTxs + "</td><td ><div id='" + timerID + "'></div></td></tr>");
    getBlockTime(time, timerID);
    updateGraph("blockTimes", [latestBlockHeight + "::" + blockTime.getHours() + ":" + blockTime.getMinutes() + ":" + blockTime.getSeconds()], [differenceBetweenBlockTime], maxNumberOfItems - 1);
    lastBlockTime = time;
    averageBlockTime = (averageBlockTime * (latestBlockHeight - 1) + differenceBetweenBlockTime) / latestBlockHeight;

    averageBlockTimeUpdater(averageBlockTime);
    updateLastBlock(latestBlockHeight, lastBlockTime);

    setCookie("latestBlockHeight", latestBlockHeight, 1);
    setCookie("lastBlockTime", lastBlockTime, 1);
    setCookie("averageBlockTime", averageBlockTime, 1);
}


function initializeBlockExplorer(blockExplorerTableBody, maxNumberOfItems) {

    const lastBlockHeightURL = jsRoutes.controllers.BlockExplorerController.lastBlockHeight();

    $.ajax({
        url: lastBlockHeightURL.url,
        type: lastBlockHeightURL.ty1,
        async: true,
        statusCode: {
            200: function (latestBlockHeightData) {
                let latestBlockHeight = parseInt(latestBlockHeightData);
                setCookie("latestBlockHeight", latestBlockHeight, 1);
                let blockDetails = jsRoutes.controllers.BlockExplorerController.blockDetails((latestBlockHeight - maxNumberOfItems), latestBlockHeight);
                $.ajax({
                    url: blockDetails.url,
                    type: blockDetails.type,
                    async: true,
                    statusCode: {
                        200: function (blockDetailsData) {
                            let blocks = JSON.parse(blockDetailsData);
                            let content = '';
                            let initialTimeData = [];
                            let initialGraphTime = [];
                            let initialGraphData = [];
                            let lastBlockTime = "";
                            Array.prototype.forEach.call(blocks.reverse(), (block, index) => {
                                let height = parseInt(block.header.height, 10);
                                let numTxs = block.header.num_txs;
                                let time = block.header.time;
                                let timerID = "timer" + (latestBlockHeight - index).toString(10);
                                let blockTime = new Date(time);
                                initialTimeData[initialTimeData.length] = time;
                                let differenceBetweenBlockTime = (blockTime.getTime() - new Date(lastBlockTime).getTime()) / 1000;
                                content = "<tr><td><button onclick='searchFunction(" + JSON.stringify(height) + ")'>" + height + "</button></td><td>" + numTxs + "</td><td><div id='" + timerID + "'></div> </td></tr>" + content;
                                lastBlockTime = time;
                                if (index > 0) {
                                    initialGraphTime[initialGraphTime.length] = height + "::" + blockTime.getHours() + ":" + blockTime.getMinutes() + ":" + blockTime.getSeconds();
                                    initialGraphData[initialGraphData.length] = differenceBetweenBlockTime;
                                }
                            });
                            let averageBlockTime = (new Date(lastBlockTime).getTime() - new Date(getCookie("firstBlockTime")).getTime()) / (1000 * (latestBlockHeight - 1));
                            averageBlockTimeUpdater(averageBlockTime);
                            updateLastBlock(latestBlockHeight, lastBlockTime);
                            $('#' + blockExplorerTableBody).prepend(content);
                            for (let i = 0; i < initialTimeData.length; i++) {
                                getBlockTime(initialTimeData[i], "timer" + (latestBlockHeight - i).toString(10));
                            }
                            updateGraph("blockTimes", initialGraphTime, initialGraphData, maxNumberOfItems);
                            lastBlockTime = initialTimeData[initialTimeData.length - 1];

                            setCookie("latestBlockHeight", latestBlockHeight, 1);
                            setCookie("lastBlockTime", lastBlockTime, 1);
                            setCookie("averageBlockTime", averageBlockTime, 1);
                        },
                        500: {}
                    }
                });
            },
            500: {}
        }
    });
}

function blockExplorerErrorEvent(event) {
    document.getElementById(blockExplorerTableBody).appendChild(document.createElement("div").innerHTML = "ERROR: " + evt.data);
}

function setFirstBlockTime() {
    let blockDetails = jsRoutes.controllers.BlockExplorerController.blockDetails(1, 1);
    $.ajax({
        url: blockDetails.url,
        type: blockDetails.type,
        async: true,
        statusCode: {
            200: function (blockDetailsData) {
                let blocks = JSON.parse(blockDetailsData);
                let firstBlockTime = blocks[0].header.time;
                setCookie("firstBlockTime", firstBlockTime, 1);
            },
        }
    });
}

$(document).ready = blockExplorer();