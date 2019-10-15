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
                    content = content + "<tr><td><p> <span id=\"text_element\" class=\"hashCode\"> "+ validator.operator_address + "</span><span onclick=\"copyToClipboard('text_element')\" class=\"tooltip\"><span class=\"tooltiptext\" id=\"myTooltip\">Copy to clipboard</span><span class='copyIcon' cmuk-icon=\"copy\" onmouseout=\"afterCopy()\"></span></span></p></td><td>" + validator.status + "</td><td >" + validator.tokens + "</div></td></td><td ><span class=\"hashCode\" title='" + validator.delegator_shares + "'>" + validator.delegator_shares + "</span></td></tr>";
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