
function loadMoreActivities(notificationRoute, tradeRoomID = null) {
    let route;
    console.log(tradeRoomID);
    if(tradeRoomID==null) {
        route = notificationRoute(($(".notificationContainer").length));
    }else {
        route = notificationRoute(($(".notificationContainer").length), tradeRoomID);
    }
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