$(document).ready(function () {

    $('#username').keyup(function () {

        let username = $(this).val();
        let usernameAvailable = $('#usernameAvailable');
        let usernameAvailableImageResult = $('#usernameAvailableImageResult');
        let usernameUnavailableImageResult = $('#usernameUnavailableImageResult');
        let usernameLoadingImageResult = $('#usernameLoadingImageResult');
        let route = jsRoutes.controllers.SignUpController.checkUsernameAvailable(username);

        if (username.length > 2) {
            usernameUnavailableImageResult.hide();
            usernameAvailableImageResult.hide();
            usernameLoadingImageResult.show();
            $.ajax({
                url: route.url,
                type: route.type,
                statusCode: {
                    200: function () {
                        usernameAvailable.prop("checked", true);
                        usernameUnavailableImageResult.hide();
                        usernameAvailableImageResult.show();
                        usernameLoadingImageResult.hide();
                    },
                    204: function () {
                        usernameAvailable.prop("checked", false);
                        usernameUnavailableImageResult.show();
                        usernameAvailableImageResult.hide();
                        usernameLoadingImageResult.hide();
                    }
                }
            });
        } else {
            usernameUnavailableImageResult.hide();
            usernameAvailableImageResult.hide();
            usernameLoadingImageResult.show();
        }
        if (username.length === 0) {
            usernameLoadingImageResult.hide();
            usernameUnavailableImageResult.hide();
            usernameAvailableImageResult.hide();
        }
    });
});

//https://stackoverflow.com/questions/11133059/play-2-x-how-to-make-an-ajax-request-with-a-common-button