function getZoneKycFilesWithGenesisLoginAction(accountID, documentTypeSequence) {
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

function getOrganizationKycFilesWithZoneLoginAction(accountID) {
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
