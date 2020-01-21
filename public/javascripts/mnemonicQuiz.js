function getQuiz() {
    let randomArray = [];
    while (randomArray.length < 3) {
        let randomNum = Math.floor(Math.random() * 24);
        if (!randomArray.includes(randomNum)) {
            randomArray.push(randomNum);
            $("#mnemonicElement-" + randomNum).attr('readonly', false).val("");
        }
    }
    $("#getQuiz").hide();
    $("#getMnemonic").show();
    $("#verifyMnemonic").show();
}

function getMnemonic(mnemonic) {
    const mnemonicElementArray = mnemonic.split(" ");
    for (let i = 0; i < 24; i++) {
        let mnemonicElement = $("#mnemonicElement-" + i);
        if (!mnemonicElement.prop('readonly')) {
            mnemonicElement.val(mnemonicElementArray[i]).attr('readonly', true);
        }
    }
    $("#getQuiz").show();
    $("#getMnemonic").hide();
    $("#verifyMnemonic").hide();
    $("#errorMnemonicInput").hide();
}

function verifyMnemonic(mnemonic) {
    const mnemonicElementArray = mnemonic.split(" ");
    for (let i = 0; i < 24; i++) {
        if ($("#mnemonicElement-" + i).val() !== mnemonicElementArray[i]) {
            $("#errorMnemonicInput").show();
            return;
        }
    }
    getForm(jsRoutes.controllers.AccountController.signUpForm(mnemonic));
}