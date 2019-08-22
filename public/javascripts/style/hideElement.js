$(document).click(function (e) {
    $('.showHide').each(function () {
        const hideElement = $(this);
        if (hideElement.is(e.target)) {
            showHide(hideElement);
        }
    });
});

function showHide(element){
    element.parent().next().slideToggle("fast");

}