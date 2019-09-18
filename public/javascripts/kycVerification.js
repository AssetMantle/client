function verifyZoneKycDocument(accountID, documentType) {
    let route = jsRoutes.controllers.AddZoneController.verifyKycDocument(accountID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('zoneKycDocumentStatus' + accountID + documentType).innerHTML = result;
            $('#buttonZoneKycDocumentStatus' + accountID + documentType).hide();
            document.getElementById('unverifiedDocumentStatus' + accountID + documentType).style.display = "none";
            document.getElementById('verifiedDocumentStatus' + accountID + documentType).style.display = "block";
        },
        error: function (error) {
            document.getElementById('zoneKycDocumentStatus' + accountID + documentType).innerHTML = error.responseText;
        }
    });
}

function verifyOrganizationKycDocument(organizationID, documentType) {
    let route = jsRoutes.controllers.AddOrganizationController.verifyKycDocument(organizationID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('organizationKycDocumentStatus' + organizationID + documentType).innerHTML = result;
            $('#buttonOrganizationKycDocumentStatus' + organizationID + documentType).hide();
            document.getElementById('unverifiedDocumentStatus' + organizationID + documentType).style.display = "none";
            document.getElementById('verifiedDocumentStatus' + organizationID + documentType).style.display = "block";
        },
        error: function (error) {
            document.getElementById('organizationKycDocumentStatus' + organizationID + documentType).innerHTML = error.responseText;
        }
    });
}

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

function organizationVerifyTraderKycDocument(traderID, documentType) {
    let route = jsRoutes.controllers.SetACLController.organizationVerifyKycDocument(traderID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('traderKycDocumentStatus' + traderID + documentType).innerHTML = result;
            $('#buttonTraderKycDocumentStatus' + traderID + documentType).hide();
            document.getElementById('organizationViewUnverifiedTraderKYCDocumentOrganizationStatus' + traderID + documentType).style.display = "none";
            document.getElementById('organizationViewVerifiedTraderKYCDocumentOrganizationStatus' + traderID + documentType).style.display = "block";
            if ($('#organizationViewUnverifiedTraderKYCDocumentZoneStatus' + traderID + documentType).hasClass("hidden") && document.getElementById('organizationViewVerifiedTraderKYCDocumentOrganizationStatus' + traderID + documentType).style.display !== "none") {
                document.getElementById('verifiedDocumentStatus' + traderID + documentType).style.display = "block";
                document.getElementById('unverifiedDocumentStatus' + traderID + documentType).style.display = "none"
            }
        },
        error: function (error) {
            document.getElementById('traderKycDocumentStatus' + traderID + documentType).innerHTML = error.responseText;
        }
    });
}

function zoneVerifyTraderKycDocument(traderID, documentType) {
    let route = jsRoutes.controllers.SetACLController.zoneVerifyKycDocument(traderID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('traderKycDocumentStatus' + traderID + documentType).innerHTML = result;
            $('#buttonTraderKycDocumentStatus' + traderID + documentType).hide();
            document.getElementById('zoneViewUnverifiedTraderKYCDocumentZoneStatus' + traderID + documentType).style.display = "none";
            document.getElementById('zoneViewVerifiedTraderKYCDocumentZoneStatus' + traderID + documentType).style.display = "block";
            if ($('#zoneViewUnverifiedTraderKYCDocumentOrganizationStatus' + traderID + documentType).hasClass("hidden") && document.getElementById('zoneViewVerifiedTraderKYCDocumentZoneStatus' + traderID + documentType).style.display !== "none") {
                document.getElementById('verifiedDocumentStatus' + traderID + documentType).style.display = "block";
                document.getElementById('unverifiedDocumentStatus' + traderID + documentType).style.display = "none"
            }
        },
        error: function (error) {
            document.getElementById('traderKycDocumentStatus' + traderID + documentType).innerHTML = error.responseText;
        }
    });
}

function rejectZoneKycDocument(accountID, documentType) {
    let route = jsRoutes.controllers.AddZoneController.rejectKycDocument(accountID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('zoneKycDocumentStatus' + accountID + documentType).innerHTML = result;
            $('#buttonZoneKycDocumentStatus' + accountID + documentType).hide();
            document.getElementById('verifiedDocumentStatus' + accountID + documentType).style.display = "none";
            document.getElementById('unverifiedDocumentStatus' + accountID + documentType).style.display = "block";
        },
        error: function (error) {
            document.getElementById('zoneKycDocumentStatus' + accountID + documentType).innerHTML = error.responseText;
        }
    });
}

function rejectOrganizationKycDocument(organizationID, documentType) {
    let route = jsRoutes.controllers.AddOrganizationController.rejectKycDocument(organizationID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('organizationKycDocumentStatus' + organizationID + documentType).innerHTML = result;
            $('#buttonOrganizationKycDocumentStatus' + organizationID + documentType).hide();
            document.getElementById('verifiedDocumentStatus' + organizationID + documentType).style.display = "none";
            document.getElementById('unverifiedDocumentStatus' + organizationID + documentType).style.display = "block";
        },
        error: function (error) {
            document.getElementById('organizationKycDocumentStatus' + organizationID + documentType).innerHTML = error.responseText;
        }
    });
}

function organizationRejectTraderKycDocument(traderID, documentType) {
    let route = jsRoutes.controllers.SetACLController.organizationRejectKycDocument(traderID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('traderKycDocumentStatus' + traderID + documentType).innerHTML = result;
            $('#buttonTraderKycDocumentStatus' + traderID + documentType).hide();
            document.getElementById('organizationViewVerifiedTraderKYCDocumentOrganizationStatus' + traderID + documentType).style.display = "none";
            document.getElementById('organizationViewUnverifiedTraderKYCDocumentOrganizationStatus' + traderID + documentType).style.display = "block";
            document.getElementById('verifiedDocumentStatus' + traderID + documentType).style.display = "none";
            document.getElementById('unverifiedDocumentStatus' + traderID + documentType).style.display = "block"
        },
        error: function (error) {
            document.getElementById('traderKycDocumentStatus' + traderID + documentType).innerHTML = error.responseText;
        }
    });
}

function zoneRejectTraderKycDocument(traderID, documentType) {
    let route = jsRoutes.controllers.SetACLController.zoneRejectKycDocument(traderID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('traderKycDocumentStatus' + traderID + documentType).innerHTML = result;
            $('#buttonTraderKycDocumentStatus' + traderID + documentType).hide();
            document.getElementById('zoneViewVerifiedTraderKYCDocumentZoneStatus' + traderID + documentType).style.display = "none";
            document.getElementById('zoneViewUnverifiedTraderKYCDocumentZoneStatus' + traderID + documentType).style.display = "block";
            document.getElementById('verifiedDocumentStatus' + traderID + documentType).style.display = "none";
            document.getElementById('unverifiedDocumentStatus' + traderID + documentType).style.display = "block"
        },
        error: function (error) {
            document.getElementById('traderKycDocumentStatus' + traderID + documentType).innerHTML = error.responseText;
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
