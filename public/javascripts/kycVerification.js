function verifyZoneKycDocument(accountID, documentType) {
    let route = jsRoutes.controllers.AddZoneController.verifyKycDocument(accountID, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('zoneKycDocumentStatus' + accountID + documentType).innerHTML = result;
            $('#buttonZoneKycDocumentStatus' + accountID + documentType).hide();
            document.getElementById('kycDocumentStatus' + accountID + documentType).innerHTML = 'true';
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
            document.getElementById('kycDocumentStatus' + accountID + documentType).innerHTML = 'true';
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
            document.getElementById('kycDocumentStatus' + accountID + documentType).innerHTML = 'true';
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
            document.getElementById('kycDocumentStatus' + accountID + documentType).innerHTML = 'true';
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
            document.getElementById('kycDocumentStatus' + accountID + documentType).innerHTML = 'false';
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
            document.getElementById('kycDocumentStatus' + accountID + documentType).innerHTML = 'false';
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
            document.getElementById('kycDocumentStatus' + accountID + documentType).innerHTML = 'false';
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
            document.getElementById('kycDocumentStatus' + accountID + documentType).innerHTML = 'false';
        },
        error: function (error) {
            document.getElementById('traderKycDocumentStatus' + accountID + documentType).innerHTML = error;
        }
    });
}