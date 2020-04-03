function noActivity() {
    if($(".recentActivityBox .notification").length==1){
        $(".recentActivityBox").html("<p class='cmuk-position-center'>No Activities Found</p>");
        $("head").append("<style>.timeline:before{height: 0%}</style>")
    }
    if ($("#chatWindow").length == 1){
        $("#chatWindow").html("<p class='cmuk-position-center'>Select chat to message</p>");
    }
}

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
                const loadMore = $(".recentActivityBox .notification:last");
                loadMore.before(data);
                loadMore.remove();
            }
        }
    });
}