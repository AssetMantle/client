
function convertUTCDateToLocalDate(date, id) {
    console.log(date)
    let newDate = new Date(date);
    console.log(newDate)
    $('#' + id).html(newDate.toLocaleDateString("en", dateFormatOptions));
}

function fromEpochDateToLocal(epochValue, id) {
    let millis = epochValue * 1000;
    let dateTime = new Date(millis);
    $('#' + id).html(dateTime.toLocaleDateString("en", dateFormatOptions));
}
