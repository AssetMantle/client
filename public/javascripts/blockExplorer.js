function blockExplorer() {
    let blockExplorerTableBody = "blockContainer";
    let wsURL = getConfiguration("blockchain.main.wsIP") + ":" + getConfiguration("blockchain.main.abciPort") + "/websocket";

    setCookie("blockExplorerCounter", 0, 1);
    setCookie("blockExplorerLastBlockTime", "", 1);
    setCookie("blockExplorerAverageBlockTime", 6.0, 1);

    let maxNumberOfItems = 8;

    window.addEventListener("load", function (evt) {
        let wsNewBlock = new WebSocket(wsURL);
        wsNewBlock.onopen = () => {
            initializeBlockExplorer(blockExplorerTableBody, maxNumberOfItems);
            initialShowAllBlocksTableContent();
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

    let counter = parseInt(getCookie("blockExplorerCounter"), 10);
    let lastBlockTime = getCookie("blockExplorerLastBlockTime");
    let averageBlockTime = parseFloat(getCookie("blockExplorerAverageBlockTime"));

    let height = parseInt(receivedData.result.data.value.block.header.height, 10);
    let numTxs = receivedData.result.data.value.block.header.num_txs;
    let time = receivedData.result.data.value.block.header.time;
    let timerID = "timer" + counter.toString(10);
    let blockTime = new Date(time);
    let differenceBetweenBlockTime = (blockTime.getTime() - new Date(lastBlockTime).getTime()) / 1000;
    let blockContainerList = document.getElementById(blockExplorerTableBody);
    blockContainerList.removeChild(blockContainerList.childNodes[blockContainerList.childNodes.length - 2]);
    $('#' + blockExplorerTableBody).prepend("<tr><td><button onclick='searchFunction(" + JSON.stringify(height) + ")'>" + height + "</button></td><td>" + numTxs + "</td><td ><div id='" + timerID + "'></div></td></tr>");
    getBlockTime(time, timerID);
    updateGraph("blockTimes", [height + "::" + blockTime.getHours() + ":" + blockTime.getMinutes() + ":" + blockTime.getSeconds()], [differenceBetweenBlockTime], maxNumberOfItems - 1);
    lastBlockTime = time;
    if (counter > 0) {
        averageBlockTime = (averageBlockTime * counter + differenceBetweenBlockTime) / (counter + 1);
    }
    counter += 1;
    averageBlockTimeUpdater(averageBlockTime);
    updateLastBlock(height, lastBlockTime);

    setCookie("blockExplorerCounter", counter, 1);
    setCookie("blockExplorerLastBlockTime", lastBlockTime, 1);
    setCookie("blockExplorerAverageBlockTime", averageBlockTime, 1);
}


function initializeBlockExplorer(blockExplorerTableBody, maxNumberOfItems) {

    let lastBlockHeightURL = jsRoutes.controllers.BlockExplorerController.lastBlockHeight();

    let counter = parseInt(getCookie("blockExplorerCounter"), 10);
    let lastBlockTime = getCookie("blockExplorerLastBlockTime");
    let averageBlockTime = parseFloat(getCookie("blockExplorerAverageBlockTime"));

    $.ajax({
        url: lastBlockHeightURL.url,
        type: lastBlockHeightURL.ty1,
        async: true,
        statusCode: {
            200: function (latestBlockHeightData) {
                let latestBlockHeight = parseInt(latestBlockHeightData);
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
                            let lastBlockHeight = 0;
                            Array.prototype.forEach.call(blocks.reverse(), block => {
                                let height = parseInt(block.header.height, 10);
                                let numTxs = block.header.num_txs;
                                let time = block.header.time;
                                let timerID = "timer" + counter.toString(10);
                                let blockTime = new Date(time);
                                initialTimeData[initialTimeData.length] = time;
                                let differenceBetweenBlockTime = (blockTime.getTime() - new Date(lastBlockTime).getTime()) / 1000;
                                content = "<tr><td><button onclick='searchFunction(" + JSON.stringify(height) + ")'>" + height + "</button></td><td>" + numTxs + "</td><td><div id='" + timerID + "'></div> </td></tr>" + content;
                                lastBlockTime = time;
                                if (counter > 0) {
                                    initialGraphTime[initialGraphTime.length] = height + "::" + blockTime.getHours() + ":" + blockTime.getMinutes() + ":" + blockTime.getSeconds();
                                    initialGraphData[initialGraphData.length] = differenceBetweenBlockTime;
                                    averageBlockTime = (averageBlockTime * counter + differenceBetweenBlockTime) / (counter + 1);
                                }
                                counter += 1;
                                lastBlockHeight = height;
                            });
                            averageBlockTimeUpdater(averageBlockTime);
                            updateLastBlock(lastBlockHeight, lastBlockTime);
                            $('#' + blockExplorerTableBody).prepend(content);
                            for (let i = 0; i < initialTimeData.length; i++) {
                                getBlockTime(initialTimeData[i], "timer" + i.toString(10));
                            }
                            updateGraph("blockTimes", initialGraphTime, initialGraphData, maxNumberOfItems - 1);
                            lastBlockTime = initialTimeData[initialTimeData.length - 1];

                            setCookie("blockExplorerCounter", counter, 1);
                            setCookie("blockExplorerLastBlockTime", lastBlockTime, 1);
                            setCookie("blockExplorerAverageBlockTime", averageBlockTime, 1);
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
window.onload = blockExplorer();