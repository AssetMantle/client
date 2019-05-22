function markNotificationRead(target, route) {
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