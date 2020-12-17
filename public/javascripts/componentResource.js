function componentResource(source, route, loadingSpinnerID = 'commonSpinner', event = '') {
    const div = $('#' + source);
    let loadingSpinner = $('#' + loadingSpinnerID);
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        global: showSpinner(event),
        beforeSend: function () {
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
                let imageElement= document.createElement('img');
                const imageRoute =jsRoutes.controllers.Assets.versioned("images/exclamation.png");
                imageElement.src= imageRoute.url;
                div.addClass("centerText");
                div.html(imageElement);
                div.append("<br>"+"<p>"+data.responseText+"</p>")

            }
        }
    });
}