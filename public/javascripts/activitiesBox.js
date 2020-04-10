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