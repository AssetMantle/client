$(document).ready(function () {
    $("#NavigationBarMenuButton").click(function () {
        $("#navigationBarLinks").slideToggle("slow");
    });
    if ($(window).width() > 639) {
        $(".mobileNotification").removeAttr("id");
    }
});
