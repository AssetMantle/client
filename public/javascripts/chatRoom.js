function loadMoreChats(chatWindowID) {
    const route = jsRoutes.controllers.TradeRoomController.loadMoreChats(chatWindowID, ($(".chatContainer").length));
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                const loadMore = $(".chatContainer .chat:first");
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
                    const loadMore = $(".chatContainer .chat:last");
                    console.log(data);
                    loadMore.after('<li class="chat sender cmuk-text-right">' + data.fromAccountID + ':' + data.message + '</li>');
                    loadMore.remove();
                    $("#MESSAGE").val("");

                },
            }
        }).fail(function (XMLHttpRequest) {
            if (XMLHttpRequest.readyState === 0) {
                $('#connectionError').fadeIn(100);
            }
        });
    }
}

function replyButton(replyToChatID) {
    // the animation login here
    $("#REPLY_TO_CHAT").val(replyToChatID);
}

function markChatRead(route) {
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                //add the persons name in read list
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

function replyMessage(source, route) {
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                //add the reply data..
                console.log(data.message);
                $(source).html(data.message);
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