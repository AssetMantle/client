function loadMoreActivities(notificationRoute, negotiationID = null) {
    let route;
    if (negotiationID === null) {
        route = notificationRoute(($(".recentActivityBox").length));
    } else {
        route = notificationRoute(($(".recentActivityBox").length), negotiationID);
    }
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                const loadMore = $(".recentActivityBox .notification:last");
                loadMore.before(data);
                loadMore.remove();
            }
        }
    });
}

$('#notificationBadge').ready(function () {
    const route = jsRoutes.controllers.NotificationController.unreadNotificationCount();
    const notificationBadge = $('#notificationBadge');
    notificationBadge.html('0');
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                notificationBadge.html(data);
            },
            204: function (data) {
            },
            401: function (data) {
            }
        }
    });
});

function markNotificationRead(target, accountID) {
    let route = jsRoutes.controllers.NotificationController.markNotificationRead(accountID);
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                $(target).addClass("read");
                $('#notificationBadge').html(data);
            }
        }
    });
}