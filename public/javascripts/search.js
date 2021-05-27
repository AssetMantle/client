function searchFunctionOnEnter(event, id) {
    if (event.keyCode === 13) {
        event.preventDefault();
        searchFunction(id);
    }
}

function searchFunction(id) {
    console.log(id, "searchFunction");
    let source = $('#' + id).val();
    const route = jsRoutes.controllers.IndexController.search(source);
    $.ajax({
        url: route.url,
        contentType: 'application/x-www-form-urlencoded',
        type: route.type,
        statusCode: {
            200: function (data) {
                replaceDocument(data);
            },
            500: function (data) {
                replaceDocument(data.responseText);
            },
            308: function (data) {
                replaceDocument(data.responseText);
            }
        }
    });
}