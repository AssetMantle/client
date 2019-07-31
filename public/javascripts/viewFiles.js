function getZoneKycFilesToGenesis(accountID) {
    let route = jsRoutes.controllers.AddZoneController.viewKycDocuments(accountID);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('kycDocuments' + accountID).innerHTML = result;
        },
        error: function (error) {
            document.getElementById('kycDocuments' + accountID).innerHTML = error;
        }
    });
}

function getOrganizationKycFilesToZone(accountID) {
        let route = jsRoutes.controllers.AddOrganizationController.viewKycDocuments(accountID);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('kycDocuments' + accountID).innerHTML = result
        },
        error: function (error) {
            document.getElementById('kycDocuments' + accountID).innerHTML = error;
        }
    });
}

function getTraderKycFilesToOrganization(accountID) {
    let route = jsRoutes.controllers.SetACLController.organizationViewKycDocuments(accountID);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('kycDocuments' + accountID).innerHTML = result
        },
        error: function (error) {
            document.getElementById('kycDocuments' + accountID).innerHTML = error;
        }
    });
}

function getTraderKycFilesToZone(accountID) {
    let route = jsRoutes.controllers.SetACLController.zoneViewKycDocuments(accountID);
    $.ajax({
        url: route.url,
        type: route.type,
        success: function (result) {
            document.getElementById('kycDocuments' + accountID).innerHTML = result
        },
        error: function (error) {
            document.getElementById('kycDocuments' + accountID).innerHTML = error;
        }
    });
}
