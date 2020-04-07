function noActivity() {
    if ($(".recentActivityBox .notification").length === 1) {
        $(".recentActivityBox").parent().html("<p class='cmuk-position-center'>No Activities Found</p>");
        $("head").append("<style>.timeline:before{height: 0%}</style>")
    }
}

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