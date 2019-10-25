$(document).ready(function () {
    $('#organizationKYCSameAsRegisteredAddress').click(function () {
        if ($(this).prop("checked") === false) {
            $('#postalAddressLine1').val("");
            $('#postalAddressLine2').val("");
            $('#postalLandmark').val("");
            $('#postalCity').val("");
            $('#postalCountry').val("");
            $('#postalZipCode').val("");
            $('#postalPhone').val("");
        } else {
            $('#postalAddressLine1').val($('#registeredAddressLine1').val());
            $('#postalAddressLine2').val($('#registeredAddressLine2').val());
            $('#postalLandmark').val($('#registeredLandmark').val());
            $('#postalCity').val($('#registeredCity').val());
            $('#postalCountry').val($('#registeredCountry').val());
            $('#postalZipCode').val($('#registeredZipCode').val());
            $('#postalPhone').val($('#registeredPhone').val());
        }
    });
});

function fillZoneIDField(zoneID) {
    $('#zoneID').val(zoneID);
}
