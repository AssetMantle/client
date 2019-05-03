let invalidBlockHeight = "";
let invalidTransactionHash = "";

function searchFunction(searchData) {
    let heightPattern = /^[0-9]*$/;
    let txHashPattern = /^[A-F0-9]{40}$/;
    let blockHeightUrl = getConfiguration("blockchain.main.ip") + ":" + getConfiguration("blockchain.main.abciPort") + "/block?height=";
    let txHashUrl = getConfiguration("blockchain.main.ip") + ":" + getConfiguration("blockchain.main.restPort") + "/txs/";

    if (searchData === undefined) {
        searchData = document.getElementById("searchValue").value;
    }
    let height = heightPattern.exec(searchData);
    if (height != null) {
        $.ajax({
            url: blockHeightUrl + height.input,
            type: "GET",
            async: true,
            statusCode: {
                200: function (heightData) {
                    if (heightData.hasOwnProperty("error")) {
                        document.getElementById('blockHeightPageHeight').innerHTML = invalidBlockHeight;
                        document.getElementById('blockHeightPageTime').innerHTML = "";
                        document.getElementById('blockHeightPageDataHash').innerHTML = "";
                        document.getElementById('blockHeightPageNumTxs').innerHTML = "";
                        document.getElementById('blockHeightPageEvidenceHash').innerHTML = "";
                        document.getElementById('blockHeightPageValidatorsHash').innerHTML = "";
                    } else {
                        document.getElementById('blockHeightPageHeight').innerHTML = heightData.result.block.header.height;
                        document.getElementById('blockHeightPageTime').innerHTML = heightData.result.block.header.time;
                        document.getElementById('blockHeightPageDataHash').innerHTML = heightData.result.block.header.data_hash;
                        document.getElementById('blockHeightPageNumTxs').innerHTML = heightData.result.block.header.num_txs;
                        document.getElementById('blockHeightPageEvidenceHash').innerHTML = heightData.result.block.header.evidence_hash;
                        document.getElementById('blockHeightPageValidatorsHash').innerHTML = heightData.result.block.header.validators_hash;
                    }
                }
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

function goBackBlockHeightSearch() {
    $('#blockHeightBottomDivision').hide();
    $('#allBlocksTable').hide();
    $('#txHashBottomDivision').hide();
    $('#validatorsTable').hide();
    $('#indexBottomDivision').show();
}

function goBackTransactionHashSearch() {
    $('#txHashBottomDivision').hide();
    $('#allBlocksTable').hide();
    $('#blockHeightBottomDivision').hide();
    $('#validatorsTable').hide();
    $('#indexBottomDivision').show();
}

function setSearchErrorValues(blockHeightError, transactionHashError) {
    invalidBlockHeight = blockHeightError;
    invalidTransactionHash = transactionHashError;
}