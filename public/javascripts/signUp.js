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
            let userNameNotAvailable = "User Name Not available";
            if (username.length > 0) {
                $.ajax({
                    url: route.url,
                    type: route.type,
                    async: true,
                    statusCode: {
                        200: function () {
                            usernameAvailableCheckBox[0].checked = true;
                            $("#checkUsernameAvailableResult span").remove();
                            $(".checkIcon").fadeIn();
                        },
                        204: function () {
                            usernameAvailableCheckBox[0].checked = false;
                            $("#checkUsernameAvailableResult span").remove();
                            $(".checkIcon").fadeOut();
                            result.append("<span class=\"userNameNotAvailable error\">" + userNameNotAvailable + "</span>");
                        },
                    }
                });
            } else {
                $("#checkUsernameAvailableResult span").remove();
            }
        }, 1500);
    }
}

function checkPasswords() {
    let confirmPassword = $('#signUpConfirmPassword').val();
    let password = $('#signUpPassword').val();
    let matchPasswordsResult = $('#matchPasswordsResult');
    let matchConfirmPasswordsResult = $('#matchConfirmPasswordsResult');
    let passwordNotMatch = "Passwords Do Not Match"
    if (confirmPassword !== password) {
        $('#matchConfirmPasswordsResult span').remove();
        matchConfirmPasswordsResult.append("<span class=\"passwordNotMatched error\">" + passwordNotMatch + "</span>");
    } else {
        $('#matchConfirmPasswordsResult span').remove();
    }
}

function showPassword() {
    $('#showPassword span').toggleClass('active');
    let password = document.getElementById("signUpPassword");
    let matchPassword = document.getElementById("signUpConfirmPassword");
    if (password.type && matchPassword.type === "password") {
        password.type = "text";
        matchPassword.type="text"
    } else {
        password.type = "password";
        matchPassword.type="password";
    }
}