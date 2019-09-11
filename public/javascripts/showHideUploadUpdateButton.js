function checkFileExists(route, uploadButtonId, updateButtonId){
    $.ajax({
        url: route.url,
        type: route.type,
        statusCode: {
            200: function () {
                $("#" + updateButtonId).show();
            },
            204: function () {
                $("#" + uploadButtonId).show();
            }
        }
    });
}

function checkFileExistsDisable(route, buttonID){
    let button = $("#" + buttonID);
    $.ajax({
        url: route.url,
        type: route.type,
        statusCode: {
            200: function () {
            },
            204: function () {
                button.prop('disabled', true);
            }
        }
    });
}