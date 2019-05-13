function getBlockTime(dateTime, timerID) {
    $(document).ready(function () {
        $("div.absent").toggleClass("present");
    });
    updateClock(dateTime, timerID);
}

function updateClock(dateTime, timerID) {
    let timerElement = document.getElementById(timerID);
    if (timerElement != null) {
        timerElement.innerHTML = format((new Date(new Date().toISOString()).getTime() - new Date(dateTime).getTime()) / 1000);
        setTimeout(function () {
            updateClock(dateTime, timerID);
        }, 1000);
    }
}

function format(time) {
    switch (true) {
        case (time < 120):
            return time + "s ago";
        case (time >= 120 && time < 60 * 120):
            return Math.floor(time / 60) + "min ago";
        case (time >= 60 * 120):
            return Math.floor((time / (60 * 60))) + "hr ago";
        default:
            return time + "s ago";
    }
}