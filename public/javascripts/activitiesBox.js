function loadMoreActivities(notificationRoute, negotiationID = null) {
    let route;
    if (negotiationID === null) {
        route = notificationRoute($(".recentActivityBox").length + 1);
    } else {
        route = notificationRoute(negotiationID, $(".recentActivityBox").length + 1);
    }
    let loadingSpinner = $('#RECENT_ACTIVITY_LOADING');
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        global: showSpinner('recentActivity'),
        beforeSend: function () {
            loadingSpinner.show();
        },
        complete: function () {
            loadingSpinner.hide();
        },
        statusCode: {
            200: function (data) {
                const loadMore = $(".recentActivityBox .notification:last");
                loadMore.after(data);
            }
        }
    });
}

$('#notificationBadge').ready(function () {
    const route = jsRoutes.controllers.NotificationController.unreadNotificationCount();
    const notificationBadge = $('#notificationBadge');
    notificationBadge.html('0');
    let loadingSpinner = $('#RECENT_ACTIVITY_LOADING');
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        global: showSpinner('recentActivity'),
        beforeSend: function () {
            loadingSpinner.show();
        },
        complete: function () {
            loadingSpinner.hide();
        },
        statusCode: {
            200: function (data) {
                notificationBadge.html(data);
            },
            204: function (data) {
            },
            401: function (data) {
            }
        }
    });
});

function markNotificationRead(target, accountID) {
    let route = jsRoutes.controllers.NotificationController.markNotificationRead(accountID);
    let loadingSpinner = $('#RECENT_ACTIVITY_LOADING');
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        global: showSpinner('recentActivity'),
        beforeSend: function () {
            loadingSpinner.show();
        },
        complete: function () {
            loadingSpinner.hide();
        },
        statusCode: {
            200: function (data) {
                $(target).addClass("read");
                $('#notificationBadge').html(data);
            }
        }
    });
}

function loadMoreActivitiesOnScroll(negotiationID) {
    $('.recentActivityMessages').on('scroll', function () {
        if (!$('.recentActivityMessages .recentActivityBox  > div').hasClass("noActivity")) {
            if ($(this).scrollTop() +
                $(this).innerHeight() >=
                $(this)[0].scrollHeight) {
                if (negotiationID) {
                    loadMoreActivities(jsRoutes.controllers.NegotiationController.tradeActivityMessages, negotiationID)
                } else {
                    loadMoreActivities(jsRoutes.controllers.NotificationController.recentActivityMessages)
                }
            }
        }
    });
}