console.log("ajksdbjkbaskjdbkja")
function getQuiz(userMnemonicsShown) {
    for (let i = 0; i < userMnemonicsShown; i++) {
        let mnemonicElement = $('#mnemonicElement-' + i);
        mnemonicElement.attr('readonly', false).val("");
        mnemonicElement.addClass('mnemonicField');
    }
    $('#quiz').hide();
    $('#getMnemonic').show();
    $('#verifyMnemonic').show();
}

function getMnemonic(userMnemonicsShown) {
    for (let i = 0; i < userMnemonicsShown; i++) {
        let mnemonicElement = $("#mnemonicElement-" + i);
        if (!mnemonicElement.prop('readonly')) {
            mnemonicElement.removeClass('mnemonicField');
            mnemonicElement.val(mnemonicElementArray[i]).attr('readonly', true);
        }
    }
    $('#quiz').show();
    $('#getMnemonic').hide();
    $('#verifyMnemonic').hide();
    $('#errorMnemonicInput').fadeOut();
}

function verifyMnemonic(mnemonics, userMnemonicsShown) {
    const mnemonicElementArray = mnemonics.split(" ");
    for (let i = 0; i < userMnemonicsShown; i++) {
        if ($("#mnemonicElement-" + i).val() !== mnemonicElementArray[i]) {
            $("#errorMnemonicInput").fadeIn();
            return;
        }
    }
    // getForm(jsRoutes.controllers.AccountController.signUpForm(mnemonic));
}