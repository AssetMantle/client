function getForm(route, modalContent = '#commonModalContent', modal = '#commonModal') {
    $.ajax({
        url: route.url,
        type: route.type,
        async: false,
        statusCode: {
            200: function (data) {
                $(modal).fadeIn(200);
                $(modalContent).html(data);
            },
            500: function (data) {
                const newDocument = document.open("text/html", "replace");
                newDocument.write(data);
                newDocument.close();
            }
        }
    });
}