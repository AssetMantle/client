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

function loadMoreNotifications() {
    const route = jsRoutes.controllers.NotificationController.notificationPage(($(".notificationContainer").length));
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                console.log(data,"in not")
                const loadMore = $(".notificationContainer .notification:last");
                loadMore.before(data);
                loadMore.remove();
            }
        }
    });
}