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
    $('#' + $.escapeSelector(source)).toggle();
}

function hideElement(element) {
    $('#' + $.escapeSelector(element)).hide();
}

function showElement(element) {
    $('#' + element$.escapeSelector(element)).show();
}

function fetchOrShowHide(source, route, e) {
    const div = $('#' + $.escapeSelector(source));
    if (!$.trim(div.html()).length) {
        componentResource(source, route);
        showHide($(e));
    }
}