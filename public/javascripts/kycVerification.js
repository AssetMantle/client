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
            document.getElementById('zoneKycDocumentStatus' + accountID + documentType).innerHTML = error;
        }
    });
}

function verifyOrganizationKycDocument(accountID, documentType) {
    let route = jsRoutes.controllers.AddOrganizationController.verifyKycDocument(accountID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('organizationKycDocumentStatus' + accountID + documentType).innerHTML = result;
            $('#buttonOrganizationKycDocumentStatus' + accountID + documentType).hide();
            document.getElementById('unverifiedDocumentStatus' + accountID + documentType).style.display = "none";
            document.getElementById('verifiedDocumentStatus' + accountID + documentType).style.display = "block";
        },
        error: function (error) {
            document.getElementById('organizationKycDocumentStatus' + accountID + documentType).innerHTML = error;
        }
    });
}

function organizationVerifyTraderKycDocument(accountID, documentType) {
    let route = jsRoutes.controllers.SetACLController.organizationVerifyKycDocument(accountID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('traderKycDocumentStatus' + accountID + documentType).innerHTML = result;
            $('#buttonTraderKycDocumentStatus' + accountID + documentType).hide();
            document.getElementById('organizationViewUnverifiedTraderKYCDocumentOrganizationStatus' + accountID + documentType).style.display = "none";
            document.getElementById('organizationViewVerifiedTraderKYCDocumentOrganizationStatus' + accountID + documentType).style.display = "block";
            if ($('#organizationViewUnverifiedTraderKYCDocumentZoneStatus' + accountID + documentType).hasClass("hidden") && document.getElementById('organizationViewVerifiedTraderKYCDocumentOrganizationStatus' + accountID + documentType).style.display !== "none") {
                document.getElementById('verifiedDocumentStatus' + accountID + documentType).style.display = "block";
                document.getElementById('unverifiedDocumentStatus' + accountID + documentType).style.display = "none"
            }
        },
        error: function (error) {
            document.getElementById('traderKycDocumentStatus' + accountID + documentType).innerHTML = error;
        }
    });
}

function zoneVerifyTraderKycDocument(accountID, documentType) {
    let route = jsRoutes.controllers.SetACLController.zoneVerifyKycDocument(accountID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('traderKycDocumentStatus' + accountID + documentType).innerHTML = result;
            $('#buttonTraderKycDocumentStatus' + accountID + documentType).hide();
            document.getElementById('zoneViewUnverifiedTraderKYCDocumentZoneStatus' + accountID + documentType).style.display = "none";
            document.getElementById('zoneViewVerifiedTraderKYCDocumentZoneStatus' + accountID + documentType).style.display = "block";
            if ($('#zoneViewUnverifiedTraderKYCDocumentOrganizationStatus' + accountID + documentType).hasClass("hidden") && document.getElementById('zoneViewVerifiedTraderKYCDocumentZoneStatus' + accountID + documentType).style.display !== "none") {
                document.getElementById('verifiedDocumentStatus' + accountID + documentType).style.display = "block";
                document.getElementById('unverifiedDocumentStatus' + accountID + documentType).style.display = "none"
            }
        },
        error: function (error) {
            document.getElementById('traderKycDocumentStatus' + accountID + documentType).innerHTML = error;
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
            document.getElementById('zoneKycDocumentStatus' + accountID + documentType).innerHTML = error;
        }
    });
}

function rejectOrganizationKycDocument(accountID, documentType) {
    let route = jsRoutes.controllers.AddOrganizationController.rejectKycDocument(accountID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('organizationKycDocumentStatus' + accountID + documentType).innerHTML = result;
            $('#buttonOrganizationKycDocumentStatus' + accountID + documentType).hide();
            document.getElementById('verifiedDocumentStatus' + accountID + documentType).style.display = "none";
            document.getElementById('unverifiedDocumentStatus' + accountID + documentType).style.display = "block";
        },
        error: function (error) {
            document.getElementById('organizationKycDocumentStatus' + accountID + documentType).innerHTML = error;
        }
    });
}

function organizationRejectTraderKycDocument(accountID, documentType) {
    let route = jsRoutes.controllers.SetACLController.organizationRejectKycDocument(accountID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('traderKycDocumentStatus' + accountID + documentType).innerHTML = result;
            $('#buttonTraderKycDocumentStatus' + accountID + documentType).hide();
            document.getElementById('organizationViewVerifiedTraderKYCDocumentOrganizationStatus' + accountID + documentType).style.display = "none";
            document.getElementById('organizationViewUnverifiedTraderKYCDocumentOrganizationStatus' + accountID + documentType).style.display = "block";
            document.getElementById('verifiedDocumentStatus' + accountID + documentType).style.display = "none";
            document.getElementById('unverifiedDocumentStatus' + accountID + documentType).style.display = "block"
        },
        error: function (error) {
            document.getElementById('traderKycDocumentStatus' + accountID + documentType).innerHTML = error;
        }
    });
}

function zoneRejectTraderKycDocument(accountID, documentType) {
    let route = jsRoutes.controllers.SetACLController.zoneRejectKycDocument(accountID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('traderKycDocumentStatus' + accountID + documentType).innerHTML = result;
            $('#buttonTraderKycDocumentStatus' + accountID + documentType).hide();
            document.getElementById('zoneViewVerifiedTraderKYCDocumentZoneStatus' + accountID + documentType).style.display = "none";
            document.getElementById('zoneViewUnverifiedTraderKYCDocumentZoneStatus' + accountID + documentType).style.display = "block";
            document.getElementById('verifiedDocumentStatus' + accountID + documentType).style.display = "none";
            document.getElementById('unverifiedDocumentStatus' + accountID + documentType).style.display = "block"
        },
        error: function (error) {
            document.getElementById('traderKycDocumentStatus' + accountID + documentType).innerHTML = error;
        }
    });
}