function cometMessageHandler(message){
    let parsedMessage = JSON.parse(JSON.stringify(message));
    switch (parsedMessage.messageType) {
        case 'ASSET' :
            if ($('#assetList').length) {
                comet('assetList', jsRoutes.controllers.ComponentViewController.assetList());
            }
            break;
        case 'FIAT' :
            if ($('#totalFiat').length) {
                $('#totalFiat').html(parsedMessage.messageContent.ping);
            }
            if ($('#fiatList').length) {
                comet('fiatList', jsRoutes.controllers.ComponentViewController.fiatList());
            }
            if ($('#traderFinancials').length) {
                comet('traderFinancials', jsRoutes.controllers.ComponentViewController.traderFinancials());
            }
            break;
        case 'ORDER':
            if ($('#orderList').length) {
                comet('orderList', jsRoutes.controllers.ComponentViewController.orderList());
            }
            break;
        case 'NEGOTIATION' :
            if ($('#buyNegotiationList').length) {
                comet('buyNegotiationList', jsRoutes.controllers.ComponentViewController.buyNegotiationList());
            }
            if ($('#sellNegotiationList').length) {
                comet('sellNegotiationList', jsRoutes.controllers.ComponentViewController.sellNegotiationList());
            }
            break;
        case 'CHAT' :
            if( $('#chatMessages').length ){
                comet('chatMessages', jsRoutes.controllers.ChatController.loadMoreChats(parsedMessage.messageContent.chatID, 0));
            }
            break;
        default :
            console.log("Unhandled Comet message", parsedMessage);
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