
function verifyTraderAssetDocument(id, documentType) {
    let route = jsRoutes.controllers.IssueAssetController.verifyAssetDocument(id, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('traderAssetDocumentStatus' + id + documentType).innerHTML = result;
            $('#buttonTraderAssetDocumentStatus' + id + documentType).hide();
            document.getElementById('unverifiedDocumentStatus' + id + documentType).style.display = "none";
            document.getElementById('verifiedDocumentStatus' + id + documentType).style.display = "block";
        },
        error: function (error) {
            document.getElementById('traderAssetDocumentStatus' + id + documentType).innerHTML = error;
        }
    });
}

function organizationVerifyTraderKYCDocument(traderID, documentType) {
    let route = jsRoutes.controllers.SetACLController.organizationVerifyKYCDocument(traderID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('traderKYCDocumentStatus' + traderID + documentType).innerHTML = result;
            $('#buttonTraderKYCDocumentStatus' + traderID + documentType).hide();
            document.getElementById('organizationViewUnverifiedTraderKYCDocumentOrganizationStatus' + traderID + documentType).style.display = "none";
            document.getElementById('organizationViewVerifiedTraderKYCDocumentOrganizationStatus' + traderID + documentType).style.display = "block";
            if ($('#organizationViewUnverifiedTraderKYCDocumentZoneStatus' + traderID + documentType).hasClass("hidden") && document.getElementById('organizationViewVerifiedTraderKYCDocumentOrganizationStatus' + traderID + documentType).style.display !== "none") {
                document.getElementById('verifiedDocumentStatus' + traderID + documentType).style.display = "block";
                document.getElementById('unverifiedDocumentStatus' + traderID + documentType).style.display = "none"
            }
        },
        error: function (error) {
            document.getElementById('traderKYCDocumentStatus' + traderID + documentType).innerHTML = error.responseText;
        }
    });
}

function zoneVerifyTraderKYCDocument(traderID, documentType) {
    let route = jsRoutes.controllers.SetACLController.zoneVerifyKYCDocument(traderID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('traderKYCDocumentStatus' + traderID + documentType).innerHTML = result;
            $('#buttonTraderKYCDocumentStatus' + traderID + documentType).hide();
            document.getElementById('zoneViewUnverifiedTraderKYCDocumentZoneStatus' + traderID + documentType).style.display = "none";
            document.getElementById('zoneViewVerifiedTraderKYCDocumentZoneStatus' + traderID + documentType).style.display = "block";
            if ($('#zoneViewUnverifiedTraderKYCDocumentOrganizationStatus' + traderID + documentType).hasClass("hidden") && document.getElementById('zoneViewVerifiedTraderKYCDocumentZoneStatus' + traderID + documentType).style.display !== "none") {
                document.getElementById('verifiedDocumentStatus' + traderID + documentType).style.display = "block";
                document.getElementById('unverifiedDocumentStatus' + traderID + documentType).style.display = "none"
            }
        },
        error: function (error) {
            document.getElementById('traderKYCDocumentStatus' + traderID + documentType).innerHTML = error.responseText;
        }
    });
}


function organizationRejectTraderKYCDocument(traderID, documentType) {
    let route = jsRoutes.controllers.SetACLController.organizationRejectKYCDocument(traderID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('traderKYCDocumentStatus' + traderID + documentType).innerHTML = result;
            $('#buttonTraderKYCDocumentStatus' + traderID + documentType).hide();
            document.getElementById('organizationViewVerifiedTraderKYCDocumentOrganizationStatus' + traderID + documentType).style.display = "none";
            document.getElementById('organizationViewUnverifiedTraderKYCDocumentOrganizationStatus' + traderID + documentType).style.display = "block";
            document.getElementById('verifiedDocumentStatus' + traderID + documentType).style.display = "none";
            document.getElementById('unverifiedDocumentStatus' + traderID + documentType).style.display = "block"
        },
        error: function (error) {
            document.getElementById('traderKYCDocumentStatus' + traderID + documentType).innerHTML = error.responseText;
        }
    });
}

function zoneRejectTraderKYCDocument(traderID, documentType) {
    let route = jsRoutes.controllers.SetACLController.zoneRejectKYCDocument(traderID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('traderKYCDocumentStatus' + traderID + documentType).innerHTML = result;
            $('#buttonTraderKYCDocumentStatus' + traderID + documentType).hide();
            document.getElementById('zoneViewVerifiedTraderKYCDocumentZoneStatus' + traderID + documentType).style.display = "none";
            document.getElementById('zoneViewUnverifiedTraderKYCDocumentZoneStatus' + traderID + documentType).style.display = "block";
            document.getElementById('verifiedDocumentStatus' + traderID + documentType).style.display = "none";
            document.getElementById('unverifiedDocumentStatus' + traderID + documentType).style.display = "block"
        },
        error: function (error) {
            document.getElementById('traderKYCDocumentStatus' + traderID + documentType).innerHTML = error.responseText;
        }
    });
}

function rejectTraderAssetDocument(id, documentType) {
    let route = jsRoutes.controllers.IssueAssetController.rejectAssetDocument(id, documentType);
    console.log('buttonTraderAssetDocumentStatus' + id + documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('traderAssetDocumentStatus' + id + documentType).innerHTML = result;
            $('#buttonTraderAssetDocumentStatus' + id + documentType).hide();
            document.getElementById('verifiedDocumentStatus' + id + documentType).style.display = "none";
            document.getElementById('unverifiedDocumentStatus' + id + documentType).style.display = "block";
        },
        error: function (error) {
            document.getElementById('traderAssetDocumentStatus' + id + documentType).innerHTML = error.responseText;
        }
    });
}
