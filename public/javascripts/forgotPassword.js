function forgotPassword(source, modalContent = '#commonModalContent', modal = '#commonModal') {
    const form = $(source).closest("form");
    const result = $(modalContent);
    $.ajax({
        type: 'POST',
        contentType: 'application/x-www-form-urlencoded',
        url: form.attr('action'),
        data: form.serialize(),
        statusCode: {
            400: function (data) {
                result.html(data.responseText);
            },
            200: function (data) {
                let forgotPasswordRoute = jsRoutes.controllers.AccountController.forgotPasswordForm(data);
                $.ajax({
                    url: forgotPasswordRoute.url,
                    type: forgotPasswordRoute.type,
                    async: false,
                    statusCode: {
                        200: function (data) {
                            result.html(data);
                        },
                        500: function (data) {
                            const newDocument = document.open("text/html", "replace");
                            newDocument.write(data);
                            newDocument.close();
                        }
                    }
                });
            },
            500: function (data) {
                result.html(data.responseText);
            }
        }
    });

}