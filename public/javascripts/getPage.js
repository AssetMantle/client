function getPage(jsRoute, elementID) {
    let route = jsRoute($('#' + $.escapeSelector(elementID)).text().replace(/^\s+|\s+$/g, ''));
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                replaceDocument(data);
            },
            500: function (data) {
                replaceDocument(data.responseText);
            },
        }
    }).fail(function (XMLHttpRequest) {
        if (XMLHttpRequest.readyState === 0) {
            $('#connectionError').fadeIn(100);
        }
    });
}