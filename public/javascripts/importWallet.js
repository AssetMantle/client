function onNext() {
    const mnemonics = $('#importWalletMnemonics').val();
    const route = jsRoutes.controllers.AccountController.checkMnemonics(mnemonics);
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function () {
                $('#mnemonicsField').hide();
                $('#usernameFields').show();
                $('#invalidMnemonicMessage').hide();
                $('#importWalletNext').hide();
                $('#importWalletBack').show();
                $('#FORM_IMPORT_WALLET_SUBMIT').show();
            },
            204: function () {
                $('#invalidMnemonicMessage').show();
                $('#mnemonicsField').show();
                $('#usernameFields').hide();
                $('#importWalletNext').show();
                $('#importWalletBack').hide();
                $('#FORM_IMPORT_WALLET_SUBMIT').hide();
            },
        }
    });
}

function onBack() {
    $('#mnemonicsField').show();
    $('#usernameFields').hide();
    $('#importWalletNext').show();
    $('#importWalletBack').hide();
    $('#FORM_IMPORT_WALLET_SUBMIT').hide();
}

$(document).ready(function () {
    $('#importWalletNext').show();
    $('#importWalletBack').hide();
    $('#FORM_IMPORT_WALLET_SUBMIT').hide();
});