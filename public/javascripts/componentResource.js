function componentResource(source, route){
    const div = $('#'+ source);
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                div.html(data);
            },
            401: function (data) {
                $('#' + source).addClass('skeleton');
                replaceDocument(data.responseText);
            },
            500: function (data) {
                replaceDocument(data.responseText);
            }
        }
    });
}