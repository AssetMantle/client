function loadMoreChats(chatWindowID) {
    const route = jsRoutes.controllers.TradeRoomController.loadMoreChats(chatWindowID, ($(".chatContainer").length));
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                const loadMore = $(".chatContainer .chatMessage:last");
                loadMore.before(data);
                loadMore.remove();
            }
        }
    });
}

function submitChatOnEnter(event, source) {
    if (event.keyCode === 13) {
        event.preventDefault();
        submitChat(source);
    }
}

function submitChat(source, target = '#chatContainer') {
    const form = $(source).closest("form");
    if (validateForm(form)) {
        const result = $(target);
        console.log(form);
        $.ajax({
            type: 'POST',
            contentType: 'application/x-www-form-urlencoded',
            url: form.attr('action'),
            data: form.serialize(),
            async: true,
            statusCode: {
                400: function (data) {

                    result.prepend(data.responseText);
                },
                500: function (data) {
                    result.prepend(data.responseText);
                },
                200: function (data) {
                    newChat(data)
                },

            }

        }).fail(function (XMLHttpRequest) {
            if (XMLHttpRequest.readyState === 0) {
                $('#connectionError').fadeIn(100);
            }
        });
    }
    scrollToTop();

}
function newChat(data) {
    const loadMore = $(".chatContainer .chatMessage:last");
    console.log(data);
    $("#MESSAGE").val("");
    $("#REPLY_TO_CHAT").val("");
    $("#replyBox").fadeOut();
    if (data.replyToID !== undefined) {
        console.log("you replies");
        loadMore.after('<li class="chatMessage sender">' +
            '<div class="you" onclick="replyButton(' + '\'' + data.id + '\'' + ',' + '\'' + data.message + '\'' + ',' + '\'' + data.fromAccountID + '\'' + ')">' +
            '<div class="messageContent replytoMessage cmuk-text-right">' +
            '<p id="' + data.replyToID + data.id + '"></p>' +
            '</div>' +
            '<p style="color:#FFFFFF">' + data.fromAccountID + ':' + data.message + '</p>' +
            '</div>' +
            '<span class="chatName">'+ data.fromAccountID.substring(0, 1)+'</span>' +
            '</li>');
        replyMessage(jsRoutes.controllers.TradeRoomController.replyToChat(data.chatWindowID, data.replyToID),data.id);
    } else {
        console.log("you no replies");
        loadMore.after('<li class="chatMessage sender">' +
            '<div class="you" onclick="replyButton(' + '\'' + data.id + '\'' + ',' + '\'' + data.message + '\'' + ',' + '\'' + data.fromAccountID + '\'' + ')">' +
            '<div class="messageContent cmuk-text-right">' +
            '<p style="color:#FFFFFF">' + data.fromAccountID + ' : ' + data.message + '</p>' +
            '</div>' +
            '</div>' +
            '<span class="chatName">'+ data.fromAccountID.substring(0, 1)+'</span>' +
            '</li>');
    }
    markChatRead(jsRoutes.controllers.TradeRoomController.markChatAsRead(data.chatWindowID))
}

function replyButton(replyToChatID, replyMessage, fromAccount) {
    // the animation login here
    console.log(replyMessage);
    $("#REPLY_TO_CHAT").val(replyToChatID);
    $("#replyBox").fadeIn();
    document.getElementById("replyAccount").innerHTML = fromAccount;
    document.getElementById("replymessage").innerHTML = replyMessage;
}

function markChatRead(route) {
    console.log(route);
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
               $('.unRead').fadeOut();
            },
            401: function (data) {

                replaceDocument(data.responseText);

            },
            500: function (data) {
                replaceDocument(data.responseText);
            }
        }
    });
}

function replyMessage(route, chatID) {
    console.log(chatID);
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                //add the reply data..
                // document.getElementById("repliedMessage").innerHTML = data.message;
                $('#' + data.id + chatID).html(data.message);
                console.log(data);
                // $(source).html(data.message);
            },
            401: function (data) {
                replaceDocument(data.responseText);
            },
            500: function (data) {
                replaceDocument(data.responseText);
            }
        }
    });
}

function scrollToTop() {
    var height = 0;
    $('#chatContainer li').each(function (i, value) {
        height += parseInt($(this).height());
    });
    height += '';
    $('#chatContainer').animate({scrollTop: height});
}

function unReadBar() {
    if ($('#unRead').length == 0) {
        console.log($('#unRead').length);
        var c=5;
        const loadMore = $(".chatContainer .chatMessage:nth-last-child(5)");
        loadMore.after('<div id="unRead" class="unRead">un read message</div>');
    } else {
    }
}

function closeReply() {
    $("#replyBox").fadeOut();
}
