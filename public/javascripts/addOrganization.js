$(document).ready(function () {
    $('#organizationKYCSameAsRegisteredAddress').click(function () {
        if ($(this).prop("checked") === false) {
            document.getElementById("postalAddressLine1").value = "";
            document.getElementById("postalAddressLine2").value = "";
            document.getElementById("postalLandmark").value = "";
            document.getElementById("postalCity").value = "";
            document.getElementById("postalCountry").value = "";
            document.getElementById("postalZipCode").value = "";
            document.getElementById("postalPhone").value = "";
        } else {
            document.getElementById("postalAddressLine1").value = document.getElementById("registeredAddressLine1").value;
            document.getElementById("postalAddressLine2").value = document.getElementById("registeredAddressLine2").value;
            document.getElementById("postalLandmark").value = document.getElementById("registeredLandmark").value;
            document.getElementById("postalCity").value = document.getElementById("registeredCity").value;
            document.getElementById("postalCountry").value = document.getElementById("registeredCountry").value;
            document.getElementById("postalZipCode").value = document.getElementById("registeredZipCode").value;
            document.getElementById("postalPhone").value = document.getElementById("registeredPhone").value;
        }
    });
});

function fillZoneIDField(zoneID) {
    $('#zoneID').val(zoneID);
}
