function cometMessageHandler(message) {
    let parsedMessage = JSON.parse(JSON.stringify(message));
    switch (parsedMessage.messageType) {
        case 'ASSET' :
            break;
        case 'FIAT' :
            if ($('#totalFiat').length) {
                $('#totalFiat').html(parsedMessage.messageContent.ping);
            }
            if ($('#traderFinancials').length) {
                comet('traderFinancials', jsRoutes.controllers.ComponentViewController.traderFinancials());
            }
            break;
        case 'ORDER':
            break;
        case 'NEGOTIATION' :
            if ($('#' + parsedMessage.messageContent.id).length) {
                if ($('#traderViewAcceptedNegotiation').length) {
                    comet('traderViewAcceptedNegotiation', jsRoutes.controllers.ComponentViewController.traderViewAcceptedNegotiation(parsedMessage.messageContent.id));
                }
                if ($('#organizationViewAcceptedNegotiation').length) {
                    comet('organizationViewAcceptedNegotiation', jsRoutes.controllers.ComponentViewController.organizationViewAcceptedNegotiation(parsedMessage.messageContent.id));
                }
                if ($('#traderViewAcceptedNegotiationDocumentList').length) {
                    comet('traderViewAcceptedNegotiationDocumentList', jsRoutes.controllers.ComponentViewController.traderViewAcceptedNegotiationDocumentList(parsedMessage.messageContent.id));
                }
            }
            break;
        case 'CHAT' :
            if ($('#chatMessages').length && $('#' + parsedMessage.messageContent.chatID).length) {
                comet('chatMessages', jsRoutes.controllers.ChatController.loadMoreChats(parsedMessage.messageContent.chatID, 0), 'CHAT_LOADING');
            }
            break;
        case 'KEEP_ALIVE' :
            break;
        default :
            console.log("Unhandled Comet message");
            break;


    }
}

function comet(source, route, loadingSpinnerID = 'commonSpinner') {
    const div = $('#' + source);
    let loadingSpinner = $('#' + loadingSpinnerID);
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        global: showSpinner('comet'),
        beforeSend: function () {
            loadingSpinner.show();
        },
        complete: function () {
            loadingSpinner.hide();
        },
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