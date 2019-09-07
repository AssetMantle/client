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

function hideElement(element) {
    $('#' + element).hide();
}

function showElement(element) {
    $('#' + element).show();
}

function deleteElementBySource(source) {
    source.parentNode.parentNode.removeChild(source.parentNode);
}

function deleteElementByID(id) {
    console.log(id);
    let element = document.getElementById(id);
    element.parentNode.removeChild(element);
}

function cloneDiv(element, to){
    console.log(element);
    console.log(to);
    $('#' + element).clone().appendTo('#' + to);
}