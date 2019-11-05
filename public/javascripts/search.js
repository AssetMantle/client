getConfigurationAsynchronously("blockchain.transaction.hashLength");

function searchOnEnter(event) {
    if (event.keyCode === 13) {
        event.preventDefault();
        searchFunction();
    }
}

function searchFunction(searchData) {
    const invalidBlockHeight = getCookie("blockHeightError");
    const invalidTransactionHash = getCookie("transactionHashError");

    const hashLength = parseInt(getConfigurationAsynchronously("blockchain.transaction.hashLength"), 10);

    const heightPattern = /^[0-9]*$/;
    //TODO pattern with config
    const txHashPattern = /^[A-F0-9]{64}$/;

    if (searchData === undefined) {
        searchData = $("#SEARCH_TX_HASH_HEIGHT").val();
    }
    let height = heightPattern.exec(searchData);
    if (height != null) {
        let blockDetails = jsRoutes.controllers.BlockExplorerController.blockDetails(height, height);
        $.ajax({
            url: blockDetails.url,
            type: blockDetails.type,
            async: true,
            statusCode: {
                200: function (blockDetailsData) {
                    let block = JSON.parse(blockDetailsData);
                    $('#blockHeightPageHeight').html(block[0].header.height);
                    $('#blockHeightPageTime').html(block[0].header.time);
                    $('#blockHeightPageDataHash').html(block[0].header.data_hash);
                    $('#blockHeightPageNumTxs').html(block[0].header.num_txs);
                    $('#blockHeightPageEvidenceHash').html(block[0].header.evidence_hash);
                    $('#blockHeightPageValidatorsHash').html(block[0].header.validators_hash);
                },
                500: {}
            }
        });
        $('#indexBottomDivision').hide();
        $('#allBlocksTable').hide();
        $('#validatorsTable').hide();
        $('#txHashBottomDivision').hide();
        $('#blockHeightBottomDivision').show();
    }
    let txHash = txHashPattern.exec(searchData);
    let transactionHashUrl = jsRoutes.controllers.BlockExplorerController.transactionHash(txHash);
    if (txHash != null) {
        $.ajax({
            url: transactionHashUrl.url,
            type: transactionHashUrl.type,
            async: true,
            statusCode: {
                200: function (transactionData) {
                    $('#txHashPageHash').html(transactionData.txhash);
                    $('#txHashPageHeight').html(transactionData.height);
                    $('#txHashPageFee').html(transactionData.tx.value.fee.gas);

                },
                500: function (data) {
                    $('#txHashPageHash').html(invalidTransactionHash);
                    $('#txHashPageHeight').html("");
                    $('#txHashPageFee').html("");
                }
            }
        });
        $('#indexBottomDivision').hide();
        $('#allBlocksTable').hide();
        $('#validatorsTable').hide();
        $('#blockHeightBottomDivision').hide();
        $('#txHashBottomDivision').show();
    }
}

function setSearchErrorValues(blockHeightError, transactionHashError) {
    setCookie("blockHeightError", blockHeightError, 1);
    setCookie("transactionHashError", transactionHashError, 1);
}