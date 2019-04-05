$(document).ready(function () {
    $('#noti_Counter')
        .css({opacity: 0})
        .text(document.getElementById("unread").getAttribute('data-value'))
        .css({top: '-10px'})
        .animate({top: '-2px', opacity: 1}, 500);

    $('#noti_Button').click(function () {

        $('#notifications').fadeToggle('fast', 'linear', function () {
            if ($('#notifications').is(':hidden')) {
                $('#noti_Button').css('background-color', '#6dae38');
            } else $('#noti_Button').css('background-color', '#6dae38');
        });

        $('#noti_Counter').fadeOut('slow');
        return false;
    });

    $("#seeAll").click(function () {
        $("#all").toggle();
    });

    $(document).click(function () {
        $('#notifications').hide();

        if ($('#noti_Counter').is(':hidden')) {
            $('#noti_Button').css('background-color', '#6dae38');
        }
    });
});

function toggle(page) {
    if (page != 1) document.getElementById("noti_Button").click()
}

function changeNotificationPage(page) {
    window.open(jsRoutes.controllers.NotificationController.changeNotificationPage(page).url, "_self")
}

function markNotificationAsRead(i, notificationID) {
    document.getElementById(i).style.backgroundColor = "#FFF";

    $.ajax({
        url: jsRoutes.controllers.NotificationController.markNotificationAsRead(notificationID).url,
        type: "GET",
        statusCode: {
            200: function (data) {
                console.log(data)
            },
            204: function (data) {
                console.log(data)
            }
        }
    });
}
