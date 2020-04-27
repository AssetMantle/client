function loadMoreActivitiesOnScroll(negotiationID) {
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
function loadMoreChatsOnScroll(chatID) {
    $('#chatMessages').on('scroll', function () {
        if (!$('#chatMessages .chatMessages > li').hasClass("noChats")) {
            var scrollTop = $(this).scrollTop();
            if (scrollTop <= 0) {
                setTimeout(function () {
                    loadMoreChats(chatID);
                    $('#chatMessages').scrollTop(100);
                }, 100);
            }
        }
    });
}