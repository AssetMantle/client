window.addEventListener("load", function (evt) {

    var txHashUrl = "http://192.168.0.107:31118/txs/";
    var ws = new WebSocket("ws://192.168.0.107:8080/tx");

    ws.onmessage = function (evt) {

        var parsed = JSON.parse(evt.data);

        $('#transactionList').prepend('<div>New Transaction:</div><div>Tx Hash:</div><a target="popup"  href=' + txHashUrl + parsed.Hash + '>' + parsed.Hash + '</a>');
    };
});