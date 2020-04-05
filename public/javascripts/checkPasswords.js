function checkPasswords() {
    let confirmPassword = $('#signUpConfirmPassword').val();
    let password = $('#signUpPassword').val();
    let matchPasswordsResult = $('#matchPasswordsResult');
    let matchConfirmPasswordsResult = $('#matchConfirmPasswordsResult');

    if (confirmPassword !== password) {
        matchPasswordsResult.html('&#10060;');
        matchConfirmPasswordsResult.html('&#10060;');
    } else {
        matchPasswordsResult.html('&#10004;');
        matchConfirmPasswordsResult.html('&#10060;');
    }
}