$(document).ready(function () {
    $('#moderated').click(function () {
        if ($(this).prop("checked") === false) {
            $('#moderatedIssueAssetRequestFormFields').show();
        }
        else {
            $('#moderatedIssueAssetRequestFormFields').hide();
        }
    });
});

