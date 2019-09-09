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
    $.ajax({
        url: route.url,
        type: route.type,
        statusCode: {
            200: function () {
                $("#" + buttonID).prop('disabled', false);
            },
            204: function () {
                $("#" + buttonID).prop('disabled', true);
                $("#" + buttonID).after("<br/>Pahile upload kar na bhadwe");
            }
        }
    });
}