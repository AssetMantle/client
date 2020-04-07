function cometMessageHandler(message){
    let parsedMessage = JSON.parse(JSON.stringify(message));
    switch (parsedMessage.messageType) {
        case 'ASSET' :
            console.log(parsedMessage);
            comet('assetList', jsRoutes.controllers.ComponentViewController.assetList());
            break;
        case 'FIAT' :

    }
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
                div.html(data)
            },
            500: function (data) {
                div.html(data)
            }
        }
    });
}