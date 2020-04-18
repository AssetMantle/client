$(document).ready(function () {
    $(window).resize(function() {
        if ($(window).width() > 1200) {
            $(".sticky").css({
                "position": "sticky",
                "top": 71,
            })
        } else {
            $(".sticky").css({
                "position": "static",
                "top": 0,
            })
        }
    });
});