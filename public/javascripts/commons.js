$(document).ready(function () {
    $("#notificationBoxCommons").load("@routes.NotificationController.showNotifications()")
});