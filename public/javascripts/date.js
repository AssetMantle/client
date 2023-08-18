function convertUTCDateToLocalDate(date, id) {
    let newDate = new Date(date);
    $('#' + id).html(newDate.toLocaleDateString("en", dateFormatOptions));
}

function fromEpochDateToLocal(epochValue, id) {
    let millis = epochValue * 1000;
    let dateTime = new Date(millis);
    $('#' + id).html(dateTime.toLocaleDateString("en", dateFormatOptions));
}

function fromNanoSecondToLocal(nano, id) {
    let millis = nano / 1000000;
    let dateTime = new Date(millis);
    $('#' + id).html(dateTime.toLocaleDateString("en", dateFormatOptions));
}
