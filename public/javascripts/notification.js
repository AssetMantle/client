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
            }
        }
    });
});
