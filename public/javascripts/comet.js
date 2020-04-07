function cometMessageHandler(message){
    let parsedMessage = JSON.parse(JSON.stringify(message));

}

function comet(source, route){
    const div = $('#'+ source);
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