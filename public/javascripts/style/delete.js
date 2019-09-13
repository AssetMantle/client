function deleteElementBySource(source) {
    source.parentNode.parentNode.removeChild(source.parentNode);
}

function deleteElementByID(id) {
    let element = document.getElementById(id);
    element.parentNode.removeChild(element);
}