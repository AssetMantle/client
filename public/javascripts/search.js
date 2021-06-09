function searchFunctionOnEnter(event, id) {
    if (event.keyCode === 13) {
        event.preventDefault();
        searchFunction(id);
    }
}

function searchFunction(id) {
    let source = $('#' + id).val();
    const route = jsRoutes.controllers.IndexController.search(source);
    componentResource('explorerContent', route);
}