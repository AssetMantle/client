function blockExplorer(wsURL, blockHeightUrl){
    window.addEventListener("load", function (evt) {
        var wsConn = new WebSocket(wsURL);
        wsConn.onmessage = function (evt) {
            var parsed = JSON.parse(evt.data);
            $('#blockList').prepend('<li><div><p><a target="popup"  href=' + blockHeightUrl + parsed.Block.Header.Height + '>Block ' + parsed.Block.Header.Height + '</a></p></div>' + '<div ><p>Number of Transaction ' + parsed.Block.Header.Num_txs + '</p> <p>Total Number of Transaction ' + parsed.Block.Header.Total_txs + '</p> </div></li>');
        }
    });
}
