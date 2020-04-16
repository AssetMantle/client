$(document).ready(function () {
    $(window).resize(function() {
        if ($(window).width() > 960) {
            $(".mobileNavigation").removeAttr("style");
        }
    });
});
function  mobileNavigation() {
    $('#mobileNavigation').slideToggle();

}