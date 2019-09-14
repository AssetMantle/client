function searchFunction(searchData) {
    let invalidBlockHeight = getCookie("blockHeightError");
    let invalidTransactionHash = getCookie("transactionHashError");

    $("#searchForm").submit(function (e) {
        e.preventDefault();
    });

    let heightPattern = /^[0-9]*$/;
    let txHashPattern = /^[A-F0-9]{40}$/;
    let txHashUrl = getConfiguration("blockchain.main.ip") + ":" + getConfiguration("blockchain.main.restPort") + "/txs/";

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
    if (txHash != null) {

        $.ajax({
            url: txHashUrl + txHash.input,
            type: "GET",
            async: true,
            statusCode: {
                200: function (data) {
                    let transactionData = JSON.parse(data);
                    document.getElementById('txHashPageHash').innerHTML = transactionData.hash;
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