function getQuiz(getQuizButton,getMnemonicButton,verifyMnemonicButton) {
    let randomArray = [];
    while (randomArray.length < 3) {
        let randomNum = Math.floor(Math.random() * 24);
        if (!randomArray.includes(randomNum)) {
            randomArray.push(randomNum);
            $("#mnemonicElement-" + randomNum).attr('readonly', false).val("");
            $("#mnemonicElement-" + randomNum).addClass('mnemonicField');
        }
    }
    $(getQuizButton).hide();
    $(getMnemonicButton).show();
    $(verifyMnemonicButton).show();
}

function getMnemonic(mnemonic,getQuizButton,getMnemonicButton,verifyMnemonicButton,errorMnemonicInput) {
    const mnemonicElementArray = mnemonic.split(" ");
    for (let i = 0; i < 24; i++) {
        let mnemonicElement = $("#mnemonicElement-" + i);
        if (!mnemonicElement.prop('readonly')) {
            $("#mnemonicElement-" +i).removeClass('mnemonicField');
            mnemonicElement.val(mnemonicElementArray[i]).attr('readonly', true);
        }
    }
    $(getQuizButton).show();
    $(getMnemonicButton).hide();
    $(verifyMnemonicButton).hide();
    $(errorMnemonicInput).hide();
}

function verifyMnemonic(mnemonic,errorMnemonicInput) {
    const mnemonicElementArray = mnemonic.split(" ");
    for (let i = 0; i < 24; i++) {
        if ($("#mnemonicElement-" + i).val() !== mnemonicElementArray[i]) {
            $(errorMnemonicInput).show();
            return;
        }
    }
    getForm(jsRoutes.controllers.AccountController.signUpForm(mnemonic));
}