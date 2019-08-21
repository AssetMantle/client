$(document).ready(function () {

    $('#confirmPassword').keyup(function () {

        let confirmPassword = $(this).val();
        let password = $('#password').val();
        let matchPasswordsImageResult = $('#matchPasswordsImageResult');

        if (confirmPassword !== password) {
            matchPasswordsImageResult.show();
        } else {
            matchPasswordsImageResult.hide();
        }
    });
});
