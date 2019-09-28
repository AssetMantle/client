function getValidators() {
    const urlGetValidators = jsRoutes.controllers.BlockExplorerController.stakingValidators();
    $.ajax({
        url: urlGetValidators.url,
        type: urlGetValidators.type,
        async: true,
        statusCode: {
            200: function (validatorListData) {
                $('#validator').html(validatorListData.length);
                let content = "";
                Array.prototype.forEach.call(validatorListData, validator => {
                    content = content + "<tr><td><p>" + validator.operator_address + "</p></td><td>" + validator.status + "</td><td >" + validator.tokens + "</div></td></td><td >" + validator.delegator_shares + "</td></tr>";
                });
                $('#validatorsTableBody').append(content);
            },
            500: {},
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

$(document).ready = getValidators();