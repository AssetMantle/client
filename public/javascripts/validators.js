getConfigurationAsynchronously("blockchain.main.ip");
getConfigurationAsynchronously("blockchain.main.restPort");

function getValidators(bodyID) {
    let urlGetValidators = getConfiguration("blockchain.main.ip") + ":" + getConfiguration("blockchain.main.restPort") + "/stake/validators";
    $.ajax({
        url: urlGetValidators,
        type: "GET",
        async: true,
        statusCode: {
            200: function (data) {
                document.getElementById(bodyID).innerHTML = "" + JSON.parse(data).length;
            }
        }
    });

}

function validatorsTable(bodyID) {
    let urlGetValidators = getConfiguration("blockchain.main.ip") + ":" + getConfiguration("blockchain.main.restPort") + "/stake/validators";
    let content = "";
    $.ajax({
        url: urlGetValidators,
        type: "GET",
        async: true,
        statusCode: {
            200: function (validatorListData) {
                Array.prototype.forEach.call(JSON.parse(validatorListData), validator => {
                    content = content + "<tr><td> <p><span id=\"text_element\" class=\"hash_code\"> " + validator["operator"] + "</span> <span onclick=\"copyToClipboard('text_element')\"> <i class=\"fa fa-clipboard\"></i></span></p></td><td>" + validator["status"] + "</td><td >" + validator["tokens"] + "</div></td></td><td >" + validator["delegator_shares"] + "</td></tr>";
                });
                $("#" + bodyID).append(content);
            }
        }
    });

}

function seeValidatorsTable() {
    $('#blockHeightBottomDivision').hide();
    $('#allBlocksTable').hide();
    $('#txHashBottomDivision').hide();
    $('#indexBottomDivision').hide();
    $('#validatorsTable').show();
}
