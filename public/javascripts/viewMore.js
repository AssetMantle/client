$(document).click(function (e) {
    $('.viewMore').each(function () {
        const elementcontent = $(this);
        if (elementcontent.is(e.target)) {
            $(this).parent().find('.cmuk-switcher').toggleClass("extendSwitcher");
            $(this).parent().toggleClass("extendCard");
        }
    });
});