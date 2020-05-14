let timer = 0;
let timeoutFlag = true;

function checkUsernameAvailable(source, resultID, usernameAvailableCheckBoxID) {
    if (timeoutFlag) {
        timeoutFlag = false;
        clearTimeout(timer);
        timer = setTimeout(function () {
            timeoutFlag = true;
            const username = $(source).val();
            const usernameAvailableCheckBox = $(usernameAvailableCheckBoxID);
            const route = jsRoutes.controllers.AccountController.checkUsernameAvailable(username);
            if (username.length > 0) {
                $.ajax({
                    url: route.url,
                    type: route.type,
                    async: true,
                    global: showSpinner('checkUsernameAvailable'),
                    statusCode: {
                        200: function () {
                            usernameAvailableCheckBox[0].checked = true;
                            $("#checkUsernameAvailableResult").fadeOut();
                            $("#checkIcon").fadeIn();
                        },
                        204: function () {
                            usernameAvailableCheckBox[0].checked = false;
                            $("#checkIcon").fadeOut();
                            $("#checkUsernameAvailableResult").fadeIn();
                        },
                    }
                });
            } else {
                $("#checkUsernameAvailableResult span, #checkIcon").hide();
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
        $('#matchConfirmPasswordsResult span').hide();
        $(".passwordNotMatched").show();
    } else {
        $('#matchConfirmPasswordsResult span').hide();
    }
}

function showPassword() {
    $('#showPassword span').toggleClass('active');
    let password = $('#signUpPassword')[0];
    let matchPassword =$('#signUpConfirmPassword')[0];
    if (password.type && matchPassword.type === "password") {
        password.type = "text";
        matchPassword.type="text"
    } else {
        password.type = "password";
        matchPassword.type="password";
    }
}