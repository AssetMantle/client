function getBlockTime(dateTime, timerID) {
    let timerElement = $('#' + timerID);
    if (timerElement != null) {
        timerElement.html(((new Date(new Date().toISOString()).getTime() - new Date(dateTime).getTime()) / 1000).toFixed(0) + "s");
    }
}

function updateBlockTimes() {
    let timeOutID = getCookie("timeOutID");
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