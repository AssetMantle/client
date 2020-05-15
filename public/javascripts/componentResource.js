function componentResource(source, route, loadingSpinnerID = 'commonSpinner', event = '') {
    const div = $('#' + source);
    let loadingSpinner = $('#' + loadingSpinnerID);
    console.log(loadingSpinnerID);
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        global: showSpinner(event),
        beforeSend: function () {
            console.log(loadingSpinnerID);
            loadingSpinner.show();
        },
        complete: function () {
            loadingSpinner.hide();
        },
        statusCode: {
            200: function (data) {
                div.html(data);
            },
            401: function (data) {
                replaceDocument(data.responseText);
            },
            500: function (data) {
                replaceDocument(data.responseText);
            }
        }
    });
}