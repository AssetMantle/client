function getForm(route, modalContent = '#commonModalContent', modal = '#commonModal') {
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                $(modal).fadeIn(200);
                $(modalContent).html(data);
            },
            302: function (data) {
                $('#commonModal').fadeOut();
                window.open(data.responseText);
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