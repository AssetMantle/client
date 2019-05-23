$(document).ready(function () {
    $('input[id="moderated"]').click(function () {
        if ($(this).prop("checked") === true) {
            $('#moderatedIssueAssetRequestFormFields').show();
        }
        else if ($(this).prop("checked") === false) {
            $('#moderatedIssueAssetRequestFormFields').hide();
        }
    });
});

