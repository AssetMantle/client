function convertUTCDateToLocalDate(date, id) {
    let newDate = new Date(date);
    let options = {
        year: "numeric",
        month: "long",
        day: "2-digit",
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    };
    document.getElementById(id).innerHTML = newDate.toLocaleDateString("en", options);
}
