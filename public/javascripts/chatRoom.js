function loadMoreChats(chatID) {
    const route = jsRoutes.controllers.ChatController.loadMoreChats(chatID, ($(".chatMessages").length));
    $.ajax({
        url: route.url,
        type: route.type,                                       
        async: true,
        statusCode: {
            200: function (data) {
                const loadMore = $(".chatMessages .chatMessage:first");
                loadMore.before(data);
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
                    $("#TEXT").val("");
                    $("#REPLY_TO_MESSAGE").val("");
                    $("#replyBox").fadeOut();
                    const loadMore = $(".chatMessages .chatMessage:last");
                    loadMore.after(data);
                    $('.noChats').remove();
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

function replyMessage(route, messageID) {
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                $('#' + data.id + messageID).html(data.text);
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
        let c=count+2;
        const loadMore = $(".chatMessages .chatMessage:nth-last-child("+c+")");
        loadMore.after('<div id="unRead" class="unRead">un read message</div>');
    } else {
    }
}

function closeReply() {
    $("#REPLY_TO_MESSAGE").val("");
    $("#replyBox").fadeOut();
}

function loadMoreChatsOnScroll(chatID) {
    $('#chatMessages').on('scroll', function () {
        if (!$('#chatMessages .chatMessages > li').hasClass("noChats")) {
            var scrollTop = $(this).scrollTop();
            if (scrollTop <= 0) {
                setTimeout(function () {
                    loadMoreChats(chatID);
                    $('#chatMessages').scrollTop(100);
                }, 100);
            }
        }
    });
}