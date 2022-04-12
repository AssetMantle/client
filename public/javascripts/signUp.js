timer = 0;
timeoutFlag = true;

function checkUsernameAvailable(source, usernameAvailableCheckBoxID) {
    if (timeoutFlag) {
        timeoutFlag = false;
        clearTimeout(timer);
        timer = setTimeout(function () {
            timeoutFlag = true;
            const username = $(source).val();
            // const usernameAvailableCheckBox = $(usernameAvailableCheckBoxID);
            const route = jsRoutes.controllers.AccountController.checkUsernameAvailable(username);
            let loadingSpinner = $('#usernameAvailableLoading');
            if (username.length > 0) {
                $.ajax({
                    url: route.url,
                    type: route.type,
                    async: true,
                    global: showSpinner('checkUsernameAvailable'),
                    beforeSend: function () {
                        loadingSpinner.show();
                    },
                    complete: function () {
                        loadingSpinner.hide();
                    },
                    statusCode: {
                        200: function () {
                            // usernameAvailableCheckBox[0].checked = true;
                            $("#checkUsernameAvailableResult").fadeOut();
                            $("#checkIcon").fadeIn();
                        },
                        204: function () {
                            // usernameAvailableCheckBox[0].checked = false;
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
    if (confirmPassword !== password) {
        $('#matchConfirmPasswordsResult span').hide();
        $(".passwordNotMatched").show();
    } else {
        $('#matchConfirmPasswordsResult span').hide();
    }
}

function showPassword() {
    $('#showPassword span').toggleClass('active');
    $('.inputField .eyeIcon.closeEye').toggleClass('hidden');
    $('.inputField .eyeIcon.openEye').toggleClass('hidden');
    let password = $('#signUpPassword')[0];
    let matchPassword = $('#signUpConfirmPassword')[0];
    if (password.type && matchPassword.type === "password") {
        password.type = "text";
        matchPassword.type = "text"
    } else {
        password.type = "password";
        matchPassword.type = "password";
    }
}

async function keplrOnSignUp(source) {
    const username = $('#signUpUsername').val();
    const [offlineSigner, address] = await getKeplrWallet();
    const signArbitraryResponse = await signArbitrary(address, username);
    $('#signUpPublicKeyType').val(signArbitraryResponse.pub_key.type);
    $('#signUpPublicKey').val(signArbitraryResponse.pub_key.value);
    $('#signUpSignature').val(signArbitraryResponse.signature);
    submitForm(source, 'commonModalContent');
}
