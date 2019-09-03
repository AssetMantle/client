function getFiles(elementID, route) {
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById(elementID).innerHTML = result
        },
        error: function (error) {
            document.getElementById(elementID).innerHTML = error;
        }
    });
}
