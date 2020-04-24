function loadMoreReachEnd(negotiationID) {
    $('.recentActivityMessages').on('scroll', function () {
        if (!$('.recentActivityMessages .recentActivityBox  > div').hasClass("noActivity")) {
            if ($(this).scrollTop() +
                $(this).innerHeight() >=
                   $(this)[0].scrollHeight) {
                if(negotiationID){
                    loadMoreActivities(jsRoutes.controllers.NegotiationController.tradeActivityMessages, negotiationID)
                }
                else {
                    loadMoreActivities(jsRoutes.controllers.NotificationController.recentActivityMessages)
                }
            }
        }
    });
}
function loadMoreActivitiesonScroll(chatID) {
    $('#chatMessages').on('scroll', function () {
        if (!$('#chatMessages .chatMessages > li').hasClass("noChats")) {
            // var srollheight = $('#chatMessages > .chatMessages').innerHeight();
            var scrollTop = $(this).scrollTop();
            if (scrollTop <= 0) {
                loadMoreChats(chatID);
                $('#chatMessages').scrollTop(100);
            }
        }
    });
}