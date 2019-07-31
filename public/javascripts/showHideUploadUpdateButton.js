function checkAccountKycFileExists(username, documentType, uploadButtonId, updateButtonId) {
    let route = jsRoutes.controllers.FileController.checkAccountKycFileExists(username, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        statusCode: {
            200: function () {
                $("#" + updateButtonId).show();
            },
            204: function () {
                $("#" + uploadButtonId).show();
            }
        }
    });
}

function checkZoneKycFileExists(username, documentType, uploadButtonId, updateButtonId) {
    let route = jsRoutes.controllers.FileController.checkZoneKycFileExists(username, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        statusCode: {
            200: function () {
                $("#" + updateButtonId).show();
            },
            204: function () {
                $("#" + uploadButtonId).show();
            }
        }
    });
}

function checkOrganizationKycFileExists(username, documentType, uploadButtonId, updateButtonId) {
    let route = jsRoutes.controllers.FileController.checkOrganizationKycFileExists(username, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        statusCode: {
            200: function () {
                $("#" + updateButtonId).show();
            },
            204: function () {
                $("#" + uploadButtonId).show();
            }
        }
    });
}

function checkTraderKycFileExists(username, documentType, uploadButtonId, updateButtonId) {
    let route = jsRoutes.controllers.FileController.checkTraderKycFileExists(username, documentType);
    $.ajax({
        url: route.url,
        type: route.type,
        statusCode: {
            200: function () {
                $("#" + updateButtonId).show();
            },
            204: function () {
                $("#" + uploadButtonId).show();
            }
        }
    });
}