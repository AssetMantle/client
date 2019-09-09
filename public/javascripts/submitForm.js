function submitForm(source, target = '#commonModalContent') {
    const form = $(source).closest("form");
    const result = $(target);
    $.ajax({
        type: 'POST',
        contentType: 'application/x-www-form-urlencoded',
        url: form.attr('action'),
        data: form.serialize(),
        async: true,
        statusCode: {
            400: function (data) {
                result.html(data.responseText);
            },
            401: function (data) {
                const newDocument = document.open("text/html", "replace");
                newDocument.write(data.responseText);
                newDocument.close();
            },
            412: function (data) {
                const newDocument = document.open("text/html", "replace");
                newDocument.write(data.responseText);
                newDocument.close();
            },
            500: function (data) {
                console.log(data);
                const newDocument = document.open("text/html", "replace");
                newDocument.write(data.responseText);
                newDocument.close();
            },
            200: function (data) {
                const newDocument = document.open("text/html", "replace");
                newDocument.write(data);
                newDocument.close();
            },
            206: function (data) {
                $(target).html(data);
            },
        }
    });
}