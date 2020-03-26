function loadMoreChats(chatWindowID) {
    const route = jsRoutes.controllers.TradeRoomController.loadMoreChats(chatWindowID, ($(".chatMessages").length));
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                const loadMore = $(".chatMessages .chatMessage:first");
                loadMore.after(data);
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

function submitChat(source, target = '#chatMessages') {
    const form = $(source).closest("form");
    if (validateForm(form)) {
        const result = $(target);
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
    const loadMore = $(".chatMessages .chatMessage:last");
    $("#MESSAGE").val("");
    $("#REPLY_TO_MESSAGE").val("");
    $("#replyBox").fadeOut();
    if (data.replyToID !== undefined) {
        loadMore.after('<li class="chatMessage sender">' +
            '<div class="you" onclick="replyButton(' + '\'' + data.id + '\'' + ',' + '\'' + data.message + '\'' + ',' + '\'' + data.fromAccountID + '\'' + ')">' +
            '<div class="messageContent replytoMessage cmuk-text-right">' +
            '<p id="' + data.replyToID + data.id + '"></p>' +
            '</div>' +
            '<p style="color:#FFFFFF">' + data.fromAccountID + ':' + data.message + '</p>' +
            '</div>' +
            '<span class="chatName">'+ data.fromAccountID.substring(0, 1)+'</span>' +
            '</li>');
        replyMessage(jsRoutes.controllers.TradeRoomController.replyToMessage(data.chatWindowID, data.replyToID),data.id);
    } else {
        loadMore.after('<li class="chatMessage sender">' +
            '<div class="you" onclick="replyButton(' + '\'' + data.id + '\'' + ',' + '\'' + data.message + '\'' + ',' + '\'' + data.fromAccountID + '\'' + ')">' +
            '<div class="messageContent cmuk-text-right">' +
            '<p style="color:#FFFFFF">' + data.fromAccountID + ' : ' + data.message + '</p>' +
            '</div>' +
            '</div>' +
            '<span class="chatName">'+ data.fromAccountID.substring(0, 1)+'</span>' +
            '</li>');
    }
}

function replyButton(replyToMessageID, replyMessage, fromAccount) {
    $("#REPLY_TO_MESSAGE").val(replyToMessageID);
    $("#replyBox").fadeIn();
    document.getElementById("replyAccount").innerHTML = fromAccount;
    document.getElementById("replymessage").innerHTML = replyMessage;
}

function markChatRead(route) {
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
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                $('#' + data.id + chatID).html(data.message);
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
    $('#chatMessages li').each(function (i, value) {
        height += parseInt($(this).height());
    });
    height += '';
    $('#chatMessages').animate({scrollTop: height});
}

function unReadBar(count) {
    if ($('#unRead').length == 0) {
        var c=count+1;
        const loadMore = $(".chatMessages .chatMessage:nth-last-child("+c+")");
        loadMore.after('<div id="unRead" class="unRead">un read message</div>');
    } else {
    }
}

function closeReply() {
    $("#replyBox").fadeOut();
}
