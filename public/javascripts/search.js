getConfigurationAsynchronously("blockchain.transaction.hashLength");

function searchFunction(searchData) {
    let invalidBlockHeight = getCookie("blockHeightError");
    let invalidTransactionHash = getCookie("transactionHashError");

    $("#searchForm").submit(function (e) {
        e.preventDefault();
    });

    let hashLength = parseInt(getConfigurationAsynchronously("blockchain.transaction.hashLength"), 10);

    let heightPattern = /^[0-9]*$/;
    //TODO pattern with config
    let txHashPattern = /^[A-F0-9]{64}$/;

    if (searchData === undefined) {
        searchData = document.getElementById("searchValue").value;
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
                    document.getElementById('blockHeightPageHeight').innerHTML = block[0].header.height;
                    document.getElementById('blockHeightPageTime').innerHTML = block[0].header.time;
                    document.getElementById('blockHeightPageDataHash').innerHTML = block[0].header.data_hash;
                    document.getElementById('blockHeightPageNumTxs').innerHTML = block[0].header.num_txs;
                    document.getElementById('blockHeightPageEvidenceHash').innerHTML = block[0].header.evidence_hash;
                    document.getElementById('blockHeightPageValidatorsHash').innerHTML = block[0].header.validators_hash;
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
                    document.getElementById('txHashPageHash').innerHTML = transactionData.txhash;
                    document.getElementById('txHashPageHeight').innerHTML = transactionData.height;
                    document.getElementById('txHashPageFee').innerHTML = transactionData.tx.value.fee.gas;

                },
                500: function (data) {
                    document.getElementById('txHashPageHash').innerHTML = invalidTransactionHash;
                    document.getElementById('txHashPageHeight').innerHTML = "";
                    document.getElementById('txHashPageFee').innerHTML = "";
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