// $('#notificationBadge').ready(function () {
//     const route = jsRoutes.controllers.NotificationController.unreadNotificationCount();
//     const notificationBadge = $('#notificationBadge');
//     notificationBadge.html('0');
//     $.ajax({
//         url: route.url,
//         type: route.type,
//         async: true,
//         statusCode: {
//             200: function (data) {
//                 notificationBadge.html(data);
//             },
//             204: function (data) {
//             },
//             401: function (data) {
//             }
//         }
//     });
// });
//
// function markNotificationRead(target, accountID) {
//     let route = jsRoutes.controllers.NotificationController.markNotificationRead(accountID);
//     $.ajax({
//         url: route.url,
//         type: route.type,
//         async: true,
//         statusCode: {
//             200: function (data) {
//                 $(target).addClass("read");
//                 $('#notificationBadge').html(data);
//             }
//         }
//     });
// }

function loadMoreChats() {
    const route = jsRoutes.controllers.TradeRoomController.chatRoom(($(".chatContainer").length));
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                const loadMore = $(".chatContainer .chat:last");
                loadMore.before(data);
                loadMore.remove();
            }
        }
    });
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
                    result.prepend(data.responseText);
                },
            }
        }).fail(function (XMLHttpRequest) {
            if (XMLHttpRequest.readyState === 0) {
                $('#connectionError').fadeIn(100);
            }
        });
    }
}