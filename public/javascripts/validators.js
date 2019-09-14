function getValidators(bodyID) {
    let urlGetValidators = jsRoutes.controllers.BlockExplorerController.stakingValidators();
    console.log("Asdas");
    $.ajax({
        url: urlGetValidators.url,
        type: urlGetValidators.type,
        async: true,
        statusCode: {
            200: function (data) {
                document.getElementById(bodyID).innerHTML = "" + JSON.parse(data).result.length;
            },
            500: {},
        }
    });
}

function validatorsTable(bodyID) {
    let urlGetValidators = jsRoutes.controllers.BlockExplorerController.stakingValidators();
    let content = "";
    $.ajax({
        url: urlGetValidators.url,
        type: urlGetValidators.type,
        async: true,
        statusCode: {
            200: function (validatorListData) {
                Array.prototype.forEach.call(JSON.parse(validatorListData).result, validator => {
                    content = content + "<tr><td> <p>" + validator.operator_address + "</p></td><td>" + validator.status + "</td><td >" + validator.tokens + "</div></td></td><td >" + validator.delegator_shares + "</td></tr>";
                });
                $("#" + bodyID).append(content);
            },
            500: {}
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
