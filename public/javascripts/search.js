function searchFunction( ){
    let mainIpAbciPort = getConfiguration("blockchain.main.ip") + ":" + getConfiguration("blockchain.main.abciPort");
    let blockHeightURL = mainIpAbciPort + "/block?height=";
    let urlQueryHash = mainIpAbciPort + "/txs/";

    var queryData = {};
    var searchData = document.getElementById("search_value").value;
    var heightPattern = /^[0-9]*$/;
    var txHashPattern = /^[A-F0-9]{40}$/;
    var height= heightPattern.exec(searchData);
    if (height!=null){
        var heightData = JSON.parse(httpGet(blockHeightURL+height.input));
        queryData["height"] = heightData.result.block.header.height;
        var date = new Date(heightData.result.block.header.time);
        queryData["block_time"] = date.toGMTString();
        queryData["hash"] = heightData.result.block_meta.block_id.hash;
        queryData["num_txs"] = heightData.result.block.header.num_txs;
    }
    var txHash = txHashPattern.exec(searchData);
    if (txHash != null){
        var txHashData=JSON.parse(httpGet(urlQueryHash+txHash.input));
        queryData["tx_hash"] = txHashData.hash;
        queryData["height"] = txHashData.height;
        queryData["fee"] = txHashData.tx.value.fee.amount[0].amount + " "+txHashData.tx.value.fee.amount[0].denom;
        queryData["gas"] = txHashData.tx.value.fee.gas;
        for (msg in txHashData.tx.value.msg){
            var msgName = Object.keys(txHashData.tx.value.msg[msg].value)[0];
            var from = txHashData.tx.value.msg[msg].value[msgName][msg].from;
            var to = txHashData.tx.value.msg[msg].value[msgName][msg].to;
            queryData ["Msgs"] = {
                "msg_name": msgName,
                "from":from,
                "to":to,
            }
        }

    }
    document.getElementById("search_result").innerHTML= JSON.stringify(queryData);
}