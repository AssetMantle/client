function processTxResponse(response) {
    const success = (response.code === 0);
    $('#txFields').hide();
    $('#formSubmitButton').hide();
    if (success) {
        $('#txSuccessful').show();
    } else {
        $('#txFailed').show();
    }
    $('#txResponse').show();
    $('#txResponseHash').html(response.transactionHash);
    $('#responseBody').show();
}

function processKeplrError(e) {
    console.log(e);
    $('#txFields').hide();
    $('#formSubmitButton').hide();
    $('#keplrErrorMessage').html(e);
    $('#keplrError').show();
    $('#responseBody').show();
}

async function sendCoinTx(source, fromAddress) {
    try {
        const toAddress = $('#sendCoinToAddress').val();
        const denom = $('#sendCoinDenom').val();
        const amount = $('#sendCoinAmount').val();
        const msg = SendMsg(fromAddress, toAddress, amount * 1000000, denom);
        const wallet = await getKeplrWallet();
        const txFee = getTxFee();
        const response = await Transaction(wallet[0], wallet[1], [msg], txFee);
        processTxResponse(response);
    } catch (e) {
       processKeplrError(e);
    }
}