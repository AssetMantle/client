$(document).ready(function () {
    $("#NavigationBarMenuButton").click(function () {
        $("#navigationBarLinks").slideToggle("slow");
    });
    if ($(window).width() < 500) {
        $('.cmuk-navbar-container').addClass('mobileNavigationBar').removeClass('cmuk-navbar-container');
        $('.cmuk-navbar').removeClass('cmuk-navbar');
        $('.cmuk-navbar-right').removeClass('cmuk-navbar-right');
        $('.cmuk-navbar-nav').removeClass('cmuk-navbar-nav');
    }
});
