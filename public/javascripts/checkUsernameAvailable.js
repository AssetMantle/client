let timer = 0;
let timeoutFlag = true;

function checkUsernameAvailable(source, resultID, usernameAvailableCheckBoxID) {
    if (timeoutFlag) {
        timeoutFlag = false;
        clearTimeout(timer);
        timer = setTimeout(function () {
            timeoutFlag = true;
            const username = $(source).val();
            const result = $(resultID);
            const usernameAvailableCheckBox = $(usernameAvailableCheckBoxID);
            const route = jsRoutes.controllers.AccountController.checkUsernameAvailable(username);

            if (username.length > 0) {
                result.html('loading');
                $.ajax({
                    url: route.url,
                    type: route.type,
                    async: true,
                    statusCode: {
                        200: function () {
                            usernameAvailableCheckBox[0].checked = true;
                            result.html('&#10004;');
                        },
                        204: function () {
                            usernameAvailableCheckBox[0].checked = false;
                            result.html('&#10060;');
                        },
                    }
                });
            } else {
                result.html('');
            }
        }, 1500);
    }
}
