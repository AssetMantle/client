$(document).scroll(function () {
    if ($(window).width() > 1200) {
        $(".sticky").css({
            "position": "sticky",
            "top": 50,
        })
    } else {
        $(".sticky").css({
            "position": "static",
            "top": 0,
        })
    }
});