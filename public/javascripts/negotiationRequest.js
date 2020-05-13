$(document).ready(function () {
    $('#FORM_NEGOTIATION_REQUEST_SUBMIT').hide();
    $('#showSelectAssetButton').hide();
    $('#showSelectCounterPartyButton').hide();
});

function showSelectAsset() {
    $('#selectCounterParty').hide();
    $('#selectAsset').show();
    $('#showSelectAssetButton').hide();
    if ($('#negotiationRequestAssetID').val() !== '') {
        $('#showSelectCounterPartyButton').show()
    }
    $('#FORM_NEGOTIATION_REQUEST_SUBMIT').hide();
}

function showSelectCounterParty() {
    $('#selectAsset').hide();
    $('#selectCounterParty').show();
    $('#showSelectAssetButton').show();
    $('#showSelectCounterPartyButton').hide();
    if ($('#negotiationRequestCounterParty').val() !== '') {
        $('#FORM_NEGOTIATION_REQUEST_SUBMIT').show()
    }
}

function selectCounterParty(traderID, traderAccountID, trderOrganizationName) {
    $('#negotiationRequestCounterParty').val(traderID);
    $('#selectedCounterPartyAccountID').html(traderAccountID);
    $('#selectedCounterPartyOrganizationName').html(trderOrganizationName);
    $('#FORM_NEGOTIATION_REQUEST_SUBMIT').show();
}

function selectAsset(assetID, assetDescription, assetType, price, quantity, shippingPeriod, portOfLoading, portOfDischarge) {
    $('#negotiationRequestAssetID').val(assetID);
    $('#selectedAssetDescription').html(assetDescription);
    $('#selectedAssetType').html(assetType);
    $('#selectedAssetPrice').html(price);
    $('#selectedAssetQuantity').html(quantity);
    $('#selectedAssetShipmentPeriod').html(shippingPeriod);
    $('#selectedAssetPortOfLoading').html(portOfLoading);
    $('#selectedAssetPortOfDischarge').html(portOfDischarge);
    $('#showSelectCounterPartyButton').show();
}