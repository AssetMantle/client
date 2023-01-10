function convertUTCDateToLocalDate(date, id) {
    let newDate = new Date(date);
    let options = {
        year: "numeric",
        month: "short",
        day: "2-digit",
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    };
    $('#' + id).html(newDate.toLocaleDateString("en", options));
}

function fromEpochDateToLocal(epochValue, id) {
    console.log(id);
    console.log(epochValue);
    let millis = epochValue * 1000;
    let dateTime = new Date(millis);
    $('#' + id).html(dateTime.toLocaleDateString() + " " + dateTime.toLocaleTimeString());
}
