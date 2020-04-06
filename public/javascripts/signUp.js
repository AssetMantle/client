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
                $.ajax({
                    url: route.url,
                    type: route.type,
                    async: true,
                    statusCode: {
                        200: function () {
                            usernameAvailableCheckBox[0].checked = true;
                            result.attr('cmuk-icon', 'check');
                        },
                        204: function () {
                            usernameAvailableCheckBox[0].checked = false;
                            result.attr('cmuk-icon', 'ban');
                        },
                    }
                });
            } else {
                result.removeAttr('cmuk-icon');
            }
        }, 1500);
    }
}

function checkPasswords() {
    let confirmPassword = $('#signUpConfirmPassword').val();
    let password = $('#signUpPassword').val();
    let matchPasswordsResult = $('#matchPasswordsResult');
    let matchConfirmPasswordsResult = $('#matchConfirmPasswordsResult');

    if (confirmPassword !== password) {
        matchPasswordsResult.attr('cmuk-icon', 'ban');
        matchConfirmPasswordsResult.attr('cmuk-icon', 'ban');
    } else {
        matchPasswordsResult.attr('cmuk-icon', 'check');
        matchConfirmPasswordsResult.attr('cmuk-icon', 'check');
    }
}