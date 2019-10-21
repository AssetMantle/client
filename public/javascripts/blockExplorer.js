getConfigurationAsynchronously("blockchain.main.wsIP");
getConfigurationAsynchronously("blockchain.main.abciPort");

function blockExplorer(blockExplorerTableBody, maxNumberOfItems) {
    let wsURL = getConfiguration("blockchain.main.wsIP") + ":" + getConfiguration("blockchain.main.abciPort") + "/websocket";

    let wsNewBlock = new WebSocket(wsURL);
    wsNewBlock.onopen = () => {
        let requestNewBlock = `{"method":"subscribe", "id":"dontcare","jsonrpc":"2.0","params":["tm.event='NewBlock'"]}`;
        wsNewBlock.send(requestNewBlock)
    };

    wsNewBlock.onmessage = function (message) {
        if ($('#indexBottomDivision').length === 0) {
            wsNewBlock.close();
        } else {
            let receivedData = JSON.parse(message.data);
            if (receivedData.result.data !== undefined) {
                updateBlockExplorer(wsNewBlock, blockExplorerTableBody, receivedData, maxNumberOfItems);
                updateShowAllBlocksTable(receivedData);
            }
        }
    };

    wsNewBlock.onerror = function (event) {
    };

    wsNewBlock.onclose = function (event) {
    };

    return wsNewBlock;
}

function updateBlockExplorer(wsNewBlock, blockExplorerTableBody, receivedData, maxNumberOfItems) {

    let lastBlockTime = getCookie("lastBlockTime");
    let averageBlockTime = parseFloat(getCookie("averageBlockTime"));
    let latestBlockHeight = parseInt(receivedData.result.data.value.block.header.height, 10);
    let numTxs = receivedData.result.data.value.block.header.num_txs;
    let time = receivedData.result.data.value.block.header.time;
    let timerID = "timer" + latestBlockHeight.toString(10);
    let blockTime = new Date(time);
    let differenceBetweenBlockTime = (blockTime.getTime() - new Date(lastBlockTime).getTime()) / 1000;
    averageBlockTime = (averageBlockTime * (latestBlockHeight - 1) + differenceBetweenBlockTime) / latestBlockHeight;
    lastBlockTime = time;

    setCookie("latestBlockHeight", latestBlockHeight, 1);
    setCookie("lastBlockTime", lastBlockTime, 1);
    setCookie("averageBlockTime", averageBlockTime, 1);

    let blockContainerList = document.getElementById(blockExplorerTableBody);
    blockContainerList.removeChild(blockContainerList.childNodes[blockContainerList.childNodes.length - 1]);
    $('#' + blockExplorerTableBody).prepend("<tr><td><button type='button' class='cmuk-button cmuk-button-text' onclick='searchFunction(" + JSON.stringify(latestBlockHeight) + ")'>" + latestBlockHeight + "</button></td><td>" + numTxs + "</td><td ><div id='" + timerID + "'></div></td></tr>");

    getBlockTime(time, timerID);
    let setTimeoutIDArray = JSON.parse(getCookie("blockExplorerTimer"));
    setTimeoutIDArray.push(timerID);
    setTimeoutIDArray.shift();
    setCookie("blockExplorerTimer", JSON.stringify(setTimeoutIDArray), 1);

    updateGraph("blockTimes", [latestBlockHeight + "::" + blockTime.getHours() + ":" + blockTime.getMinutes() + ":" + blockTime.getSeconds()], [differenceBetweenBlockTime], maxNumberOfItems - 1);

    averageBlockTimeUpdater(averageBlockTime);
    updateLastBlock(latestBlockHeight, lastBlockTime);

}

function initializeBlockExplorer(blockExplorerTableBody, maxNumberOfItems) {

    const lastBlockHeightURL = jsRoutes.controllers.BlockExplorerController.lastBlockHeight();
    let setTimeoutIDArray = [""];
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
                                content = "<tr><td><button type='button' class='cmuk-button cmuk-button-text' onclick='searchFunction(" + JSON.stringify(height) + ")'>" + height + "</button></td><td>" + numTxs + "</td><td><div id='" + timerID + "'></div> </td></tr>" + content;
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
                                setTimeoutIDArray.push("timer" + (latestBlockHeight - i).toString(10));
                            }
                            setTimeoutIDArray.shift();
                            setCookie("blockExplorerTimer", JSON.stringify(setTimeoutIDArray), 1);

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

function getBlockTime(dateTime, timerID) {
    let timerElement = $('#' + timerID);
    if (timerElement != null) {
        timerElement.html(((new Date(new Date().toISOString()).getTime() - new Date(dateTime).getTime()) / 1000).toFixed(0) + "s");
    }
}

function updateBlockTimes() {
    let timeOutID = parseInt(getCookie("timeOutID"), 10);
    clearTimeout(timeOutID);
    let setTimeoutIDArray = JSON.parse(getCookie("blockExplorerTimer"));
    if (setTimeoutIDArray !== undefined) {
        $.each(setTimeoutIDArray, function (index, value) {
            let timerElement = $('#' + value);
            if (timerElement.html() !== undefined) {
                timerElement.html((parseInt(timerElement.html().match(/\d+/)[0], 10) + 1) + "s");
            }
        });
    }
    timeOutID = setTimeout(function () {
        updateBlockTimes();
    }, 1000);
    setCookie("timeOutID", timeOutID, 1);
}

$('#indexBottomDivision').ready(function () {
    const blockExplorerTableBody = "blockContainer";
    const maxNumberOfItems = 8;
    setFirstBlockTime();
    initializeBlockExplorer(blockExplorerTableBody, maxNumberOfItems);
    showAllBlocksInitialTableContent();
    updateBlockTimes();
    let wsNewBlock = blockExplorer(blockExplorerTableBody, maxNumberOfItems);
    $(window).on('replace', function (e) {
        wsNewBlock.close();
    });
});
