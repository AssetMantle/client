function getForm(route, modalContent = '#commonModalContent', modal = '#commonModal') {
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                $(modalContent).html(data);
                $(modal).fadeIn(200);
            },
            500: function (data) {
                replaceDocument(data.responseText);
            },
        }
    }).fail(function (XMLHttpRequest) {
        if (XMLHttpRequest.readyState === 0) {
            $('#connectionError').fadeIn(300);
        }
    });
}