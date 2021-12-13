function convertUTCDateToLocalDate(date) {
    let newDate = new Date(date);
    let options = {
        year: "numeric",
        month: "long",
        day: "2-digit",
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    };
    return newDate.toLocaleDateString("en", options)
}