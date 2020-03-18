
function loadMoreActivities(notificationRoute, tradeRoomID = null) {
    let route;
    console.log(tradeRoomID);
    if(tradeRoomID==null) {
        route = notificationRoute(($(".recentActivityBox").length));
    }else {
        route = notificationRoute(($(".recentActivityBox").length), tradeRoomID);
    }
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                console.log(data)
                const loadMore = $(".recentActivityBox .notification:last");
                loadMore.before(data);
                loadMore.remove();
            }
        }
    });
}