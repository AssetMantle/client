function cometMessageHandler(message){
    let parsedMessage = JSON.parse(JSON.stringify(message));
    console.log(parsedMessage);

    switch (parsedMessage.messageType) {
        case 'ASSET' :
            comet('assetList', jsRoutes.controllers.ComponentViewController.assetList());
            break;
        case 'FIAT' :
            $('#totalFiat').html(parsedMessage.messageContent.Fiat.ping);
            comet('fiatList', jsRoutes.controllers.ComponentViewController.fiatList());
            comet('traderFinancials', jsRoutes.controllers.ComponentViewController.traderFinancials());
            break;
        case 'ORDER':
            comet('orderList', jsRoutes.controllers.ComponentViewController.orderList());
            break;
        case 'NEGOTIATION' :
            comet('buyNegotiationList', jsRoutes.controllers.ComponentViewController.buyNegotiationList());
            comet('sellNegotiationList', jsRoutes.controllers.ComponentViewController.sellNegotiationList());
            break;
        case 'CHAT' :
            comet('chatMessages', jsRoutes.controllers.ChatController.loadMoreChats(parsedMessage.messageContent.Message.chatID, 0));
            break;
        default :
            console.log("Unhandled");
            break;


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