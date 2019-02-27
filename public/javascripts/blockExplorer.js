window.addEventListener("load", function (evt) {

    var blockHeightUrl = "http://localhost:36657/block?height=";
    var ws = new WebSocket("ws://localhost:2259/block");

    ws.onmessage = function (evt) {

        var parsed = JSON.parse(evt.data);

        $('#blockList').prepend('<li><div><p><a target="popup"  href=' + blockHeightUrl + parsed.Block.Header.Height + '>Block ' + parsed.Block.Header.Height + '</a></p></div>' + '<div ><p>Number of Transaction ' + parsed.Block.Header.Num_txs + '</p> <p>Total Number of Transaction ' + parsed.Block.Header.Total_txs + '</p> </div></li>');
    }
});