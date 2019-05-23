function loadMoreNotifications() {
    const route = jsRoutes.controllers.NotificationController.notificationPage(($(".notificationContainer").length));
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                const loadMore = $(".notificationContainer .notification:last");
                loadMore.before(data);
                loadMore.remove();
            }
        }
    });
}