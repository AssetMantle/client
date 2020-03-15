function componentResource(source, route){
    const div = $('#'+ source);
    console.log('\n\n\n');
    console.log(source);
    console.log(route);
    console.log(JSON.stringify(route));
    console.log('\n\n\n');
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                div.html(data)
            },
            401: function (data) {
                const newDocument = document.open("text/html", "replace");
                newDocument.write(data);
                newDocument.close();
            },
            500: function (data) {
                const newDocument = document.open("text/html", "replace");
                newDocument.write(data);
                newDocument.close();
            }
        }
    });
}