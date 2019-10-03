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

function showHideByID(source){
    $('#' + source).toggle();
}

function hideElement(element) {
    $('#' + element).hide();
}

function showElement(element) {
    $('#' + element).show();
}

function fetchOrShowHide(source, route, e) {
    const div = $('#' + source);
    if (!$.trim(div.html()).length) {
        componentResource(source, route);
        showHide($(e));
    }
}