$(document).ready(function () {
    $('#unmoderated').click(function () {
        if ($(this).prop("checked") === true) {
            $('#unmoderatedIssueAssetRequestFormFields').show();
        }
        else if ($(this).prop("checked") === false) {
            $('#unmoderatedIssueAssetRequestFormFields').hide();
        }
    });
});

