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

