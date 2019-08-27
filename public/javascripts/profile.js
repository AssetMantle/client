$(document).ready(function () {
    let profilePictureRoute = jsRoutes.controllers.ComponentViewController.profilePicture();
    let conatactRoute = jsRoutes.controllers.ContactController.getContact();
    let profileDocumentsRoute = jsRoutes.controllers.ComponentViewController.profileDocuments();

    $.ajax({
        url: profilePictureRoute.url,
        type: profilePictureRoute.type,
        statusCode: {
            200: function (data) {
                document.getElementById('profilePicture').innerHTML = data;
            },
            204: function (data) {
            }
        }
    });

    $.ajax({
        url: conatactRoute.url,
        type: conatactRoute.type,
        statusCode: {
            200: function (data) {
                document.getElementById('emailAddressValue').innerText = data.emailAddress;
                document.getElementById('phoneValue').innerText = data.mobileNumber;
                if (data.emailAddressVerified) {
                    $('#emailAddressValueVerifiedImage').show();
                } else {
                    $('#emailAddressValueUnverifiedImage').show();
                    $('#verifyEmailAddress').show();
                }
                if (data.mobileNumberVerified) {
                    $('#phoneValueVerifiedImage').show();
                } else {
                    $('#phoneValueUnverifiedImage').show();
                    $('#verifyPhone').show();
                }
            },
            204: function (data) {
                $('#updateContact').show();
            }
        }
    });

    $.ajax({
        url: profileDocumentsRoute.url,
        type: profileDocumentsRoute.type,
        statusCode: {
            200: function (data) {
                document.getElementById('profileDocuments').innerHTML = data;
            },
            204: function (data) {
            }
        }
    });
});
