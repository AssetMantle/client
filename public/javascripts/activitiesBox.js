function loadMoreActivities(notificationRoute) {
    let route = notificationRoute($(".recentActivityBox").length + 1);
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

// $('#notificationBadge').ready(function () {
//     const route = jsRoutes.controllers.NotificationController.unreadNotificationCount();
//     const notificationBadge = $('#notificationBadge');
//     notificationBadge.html('0');
//     let loadingSpinner = $('#RECENT_ACTIVITY_LOADING');
//     $.ajax({
//         url: route.url,
//         type: route.type,
//         async: true,
//         global: showSpinner('recentActivity'),
//         beforeSend: function () {
//             loadingSpinner.show();
//         },
//         complete: function () {
//             loadingSpinner.hide();
//         },
//         statusCode: {
//             200: function (data) {
//                 notificationBadge.html(data);
//             },
//             204: function (data) {
//             },
//             401: function (data) {
//             }
//         }
//     });
// });

// function markNotificationRead(target, accountID) {
//     let route = jsRoutes.controllers.NotificationController.markNotificationRead(accountID);
//     let loadingSpinner = $('#RECENT_ACTIVITY_LOADING');
//     $.ajax({
//         url: route.url,
//         type: route.type,
//         async: true,
//         global: showSpinner('recentActivity'),
//         beforeSend: function () {
//             loadingSpinner.show();
//         },
//         complete: function () {
//             loadingSpinner.hide();
//         },
//         statusCode: {
//             200: function (data) {
//                 $(target).addClass("read");
//                 $('#notificationBadge').html(data);
//             }
//         }
//     });
// }

window.addEventListener('load', (event) => {
    var el = document.getElementById("recentActivityMessages")
    if(el) {
        el.addEventListener("mouseover", function(event) {
            console.log(el);
            loadMoreActivitiesOnScroll()
        });
    }
});


function loadMoreActivitiesOnScroll() {
    console.log("Reaching")
    if (!$('.recentActivityMessages .recentActivityBox  > div').hasClass("noActivity")) {
        loadMoreActivities(jsRoutes.controllers.NotificationController.recentActivityMessages)
    }
}
function onNotificationClick(route, inNewPage) {
    if (route !== "") {
        if (inNewPage) {
            $.ajax({
                url: route.url,
                type: route.type,
                async: true,
                statusCode: {
                    200: function (data) {
                        replaceDocument(data);
                    },
                    500: function (data) {
                        replaceDocument(data.responseText);
                    },
                }
            }).fail(function (XMLHttpRequest) {
                if (XMLHttpRequest.readyState === 0) {
                    $('#connectionError').fadeIn(100);
                }
            });
        } else {
            // For explorer, modals aren't required rather centre cards needs to be updated
            // getForm(route);

            componentResource('explorerContent', route);
        }
    }
}