getConfigurationAsynchronously("blockchain.main.wsIP");
getConfigurationAsynchronously("blockchain.main.ip");
getConfigurationAsynchronously("blockchain.main.abciPort");

function blockExplorer(){
    let maxNumberOfItems = 8;
    let blockExplorerTableBody = "blockContainer";
    let wsURL = getConfiguration("blockchain.main.wsIP") + ":" + getConfiguration("blockchain.main.abciPort") + "/websocket";
    let mainIpAbciPort = getConfiguration("blockchain.main.ip") + ":" + getConfiguration("blockchain.main.abciPort");
    let abciInfoURL = mainIpAbciPort + "/abci_info";

    let counter = 0;
    let lastBlockTime = "";
    let averageBlockTime = 6.0;

    $.ajax({
        url: abciInfoURL,
        type: "GET",
        async: true,
        statusCode: {
            200: function (latestBlockHeightData) {
                let latestBlockHeight = parseInt(latestBlockHeightData.result.response.last_block_height);
                let urlMinMax = mainIpAbciPort + "/blockchain?minHeight=" + (latestBlockHeight - maxNumberOfItems).toString(10) + "&maxHeight=" + latestBlockHeight.toString(10);
                $.ajax({
                    url: urlMinMax,
                    type: "GET",
                    async: true,
                    statusCode: {
                        200: function (urlMinMaxData) {
                            let blocks = urlMinMaxData.result.block_metas;
                            let content = '';
                            let initialTimeData = [];
                            let initialGraphTime = [];
                            let initialGraphData = [];
                            let lastBlockHeight = 0;
                            Array.prototype.forEach.call(blocks.reverse(), block => {
                                let height = parseInt(block["header"]["height"], 10);
                                let numTxs = block["header"]["num_txs"];
                                let time = block["header"]["time"];
                                let timerID = "timer" + counter.toString(10);
                                let blockTime = new Date(time);
                                initialTimeData[initialTimeData.length] = time;
                                let differenceBetweenBlockTime = ( blockTime.getTime() - new Date(lastBlockTime).getTime()) / 1000;
                                content = "<tr><td><button onclick='searchFunction("+ JSON.stringify(height) +")'>" + height + "</button></td><td>" + numTxs + "</td><td><div id='" + timerID + "'></div> </td></tr>" + content;
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
                            updateLastBlock(abciInfoURL, lastBlockHeight, lastBlockTime);
                            $('#' + blockExplorerTableBody).prepend(content);
                            for (let i = 0; i < initialTimeData.length; i++) {
                                getBlockTime(initialTimeData[i], "timer" + i.toString(10));
                            }
                            updateGraph("blockTimes", initialGraphTime, initialGraphData, maxNumberOfItems -1);
                            lastBlockTime = initialTimeData[initialTimeData.length - 1];

                            window.addEventListener("load", function (evt) {
                                let wsNewBlock = new WebSocket(wsURL);
                                wsNewBlock.onopen = () => {
                                    let requestNewBlock = `{"method":"subscribe", "id":"dontcare","jsonrpc":"2.0","params":["tm.event='NewBlock'"]}`;
                                    wsNewBlock.send(requestNewBlock)
                                };

                                wsNewBlock.onmessage = function (message) {
                                    let dataNewBlock = JSON.parse(message.data);
                                    let height = parseInt(dataNewBlock["result"]["data"]["value"]["block"]["header"]["height"], 10);
                                    let numTxs = dataNewBlock["result"]["data"]["value"]["block"]["header"]["num_txs"];
                                    let time = dataNewBlock["result"]["data"]["value"]["block"]["header"]["time"];
                                    let timerID = "timer" + counter.toString(10);
                                    let blockTime = new Date(time);
                                    let differenceBetweenBlockTime = (blockTime.getTime() - new Date(lastBlockTime).getTime()) / 1000;
                                    let blockContainerList = document.getElementById(blockExplorerTableBody);
                                    blockContainerList.removeChild(blockContainerList.childNodes[blockContainerList.childNodes.length - 2]);
                                    $('#' + blockExplorerTableBody).prepend("<tr><td><button onclick='searchFunction("+ JSON.stringify(height) +")'>" + height + "</button></td><td>" + numTxs + "</td><td ><div class='timer_div' id='" + timerID + "'></div></td></tr>");
                                    getBlockTime(time, timerID);
                                    updateGraph("blockTimes", [height + "::" + blockTime.getHours() + ":" + blockTime.getMinutes() + ":" + blockTime.getSeconds()], [differenceBetweenBlockTime], maxNumberOfItems - 1);
                                    lastBlockTime = time;
                                    if (counter > 0) {
                                        averageBlockTime = (averageBlockTime * counter + differenceBetweenBlockTime) / (counter + 1);
                                    }
                                    counter += 1;
                                    averageBlockTimeUpdater(averageBlockTime);
                                    updateLastBlock(abciInfoURL, height, lastBlockTime);
                                };

                                wsNewBlock.onerror = function (evt) {
                                    document.getElementById(blockExplorerTableBody).appendChild(document.createElement("div").innerHTML = "ERROR: " + evt.data);
                                };
                            });
                        }
                    }
                });
            }
        }
    });
}

window.onload = blockExplorer();