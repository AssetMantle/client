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
            let userNameNotAvailable = "";
            if (username.length > 0) {
                $.ajax({
                    url: route.url,
                    type: route.type,
                    async: true,
                    statusCode: {
                        200: function () {
                            usernameAvailableCheckBox[0].checked = true;
                            $("#checkUsernameAvailableResult span").hide();
                            $("#checkIcon").fadeIn();
                        },
                        204: function () {
                            usernameAvailableCheckBox[0].checked = false;
                            $("#checkUsernameAvailableResult span").hide();
                            $("#checkIcon").fadeOut();
                            $("#checkUsernameAvailableResult .userNameNotAvailable").show();
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