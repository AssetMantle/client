$(document).ready(function () {
    if ($(window).width() > 1200) {
        $(".sticky").css({
            "position": "sticky",
            "top": 71,
        })
    } else {
        $(window).resize(function() {
            $(".sticky").css({
                "position": "static",
                "top": 0,
            })
        });
    }
});