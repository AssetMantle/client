$(document).ready(function () {
    $('#SAME_AS_REGISTERED_ADDRESS').click(function () {
        if ($(this).prop("checked") === false) {
            $('#POSTAL_ADDRESS_ADDRESS_LINE_1').val("");
            $('#POSTAL_ADDRESS_ADDRESS_LINE_2').val("");
            $('#POSTAL_ADDRESS_LANDMARK').val("");
            $('#POSTAL_ADDRESS_CITY').val("");
            $('#POSTAL_ADDRESS_COUNTRY').val("");
            $('#POSTAL_ADDRESS_ZIP_CODE').val("");
            $('#POSTAL_ADDRESS_PHONE').val("");
        } else {
            $('#POSTAL_ADDRESS_ADDRESS_LINE_1').val($('#REGISTERED_ADDRESS_ADDRESS_LINE_1').val());
            $('#POSTAL_ADDRESS_ADDRESS_LINE_2').val($('#REGISTERED_ADDRESS_ADDRESS_LINE_2').val());
            $('#POSTAL_ADDRESS_LANDMARK').val($('#REGISTERED_ADDRESS_LANDMARK').val());
            $('#POSTAL_ADDRESS_CITY').val($('#REGISTERED_ADDRESS_CITY').val());
            $('#POSTAL_ADDRESS_COUNTRY').val($('#REGISTERED_ADDRESS_COUNTRY').val());
            $('#POSTAL_ADDRESS_ZIP_CODE').val($('#REGISTERED_ADDRESS_ZIP_CODE').val());
            $('#POSTAL_ADDRESS_PHONE').val($('#REGISTERED_ADDRESS_PHONE').val());
        }
    });
});

function fillZoneIDField(zoneID) {
    $('#ZONE_ID').val(zoneID);
}
