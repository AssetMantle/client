$(document).ready(function () {
    $('input[id="moderated"]').click(function () {
        if ($(this).prop("checked") === true) {
            $('#issueAssetRequestForm').show();
        }
        else if ($(this).prop("checked") === false) {
            $('#issueAssetRequestForm').hide();
        }
    });
});

