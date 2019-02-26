function transactionExplorer(wsUrl, txHashUrl){
window.addEventListener("load", function (evt) {
    var wsConn = new WebSocket(wsUrl);
    wsConn.onmessage = function (evt) {
        var parsed = JSON.parse(evt.data);
        $('#transactionList').prepend('<div>New Transaction:</div><div>Tx Hash:</div><a target="popup"  href=' + txHashUrl + parsed.Hash + '>' + parsed.Hash + '</a>');
    };
});
}