$(document).ready(function () {
    let route = jsRoutes.controllers.AddOrganizationController.checkAllOrganizationKYCFilesExists;
    $.ajax({
        url: route.url,
        type: route.type,
        statusCode: {
            200: function () {
                showElement('getOrganizationKYCReviewForm');
            },
            204: function () {
                hideElement('getOrganizationKYCReviewForm');
            }
        }
    });
});
