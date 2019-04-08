$(document).ready(function () {
    $('#notificationCounter')
        .css({opacity: 0})
        .text(document.getElementById("unread").getAttribute('data-value'))
        .css({top: '-10px'})
        .animate({top: '-2px', opacity: 1}, 500);

    $("#seeAll").click(function () {
        $("#all").toggle();
    });

    $(document).on('click', function (e) {
        if (e.target.id === 'notificationButton') {
            $('#notifications').fadeToggle('fast', 'linear', function () {
                $('#notificationButton').css('background-color', '#6dae38');
            });
            return false;
        }
        $('#notifications').hide();

        if ($('#notificationCounter').is(':hidden')) {
            $('#notificationButton').css('background-color', '#6dae38');
        }
    });
});

function changeNotificationPage(page) {
    var route = jsRoutes.controllers.NotificationController.showNotifications(page);
    document.getElementById("notificationWindowCommons").innerHTML = "loading";

    $.ajax({
        url: route.url,
        type: route.type,
        success: function (data) {
            document.getElementById("notificationWindowCommons").innerHTML = data;
            document.getElementById("notificationCounter").textContent = document.getElementById("unread").getAttribute('data-value');
            document.getElementById("notificationButton").click();
        },
        error: function (data) {
            result.innerHTML = data
        }
    });
}

function markNotificationAsRead(i, notificationID) {
    document.getElementById(i).style.backgroundColor = "#FFF";
    $.ajax({
        url: jsRoutes.controllers.NotificationController.markNotificationAsRead(notificationID).url,
        type: "GET",
        statusCode: {
            200: function (data) {
                document.getElementById("notificationButton").click();
                document.getElementById("notificationCounter").textContent = data
            }
        }
    });
}
