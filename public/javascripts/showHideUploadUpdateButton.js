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
