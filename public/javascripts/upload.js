function getFileTypes(documentType) {
    let fileTypes = [];
    switch (documentType) {
        case "BANK_DETAILS":
        case "IDENTIFICATION":
            fileTypes = ['jpg', 'png', 'jpeg', 'pdf', 'doc', 'txt', 'docx'];
            break;
        case "PROFILE_PICTURE":
            fileTypes = ['jpg', 'png', 'jpeg'];
            break;
        default:
            fileTypes = ['jpg', 'png', 'jpeg'];
            break;
    }
    return fileTypes
}

function uploadFile(uploadRoute, storeRoute, documentType) {
    console.log(uploadRoute, storeRoute, documentType);
    const rFile = new Resumable({
        target: uploadRoute(documentType).url,
        fileType: getFileTypes(documentType),
        query: {csrfToken: $('[name="csrfToken"]').attr('value')}
    });

    rFile.assignBrowse(document.getElementById('browseUploadButton'));

    rFile.assignDrop(document.getElementById('uploadSelector'));

    rFile.on('fileAdded', function (file) {
        $("#uploader").show();
        $("#uploadSelector").hide();
        $("#fileName").html(file.fileName);
    });

    rFile.on('fileProgress', function (file) {
        moveProgressBar($("#fileUploadProgressBar"), (file.progress(false) * 100.00));
    });

    let uploadCompletionMessage = document.getElementById('uploadCompletionMessage');
    rFile.on('fileSuccess', function (file) {
        $("#uploadControls").delay(1000).fadeOut(1000);
        let storeDbRoute = storeRoute(file.fileName, documentType);
        $.ajax({
            url: storeDbRoute.url,
            type: storeDbRoute.type,
            success: function (result) {
                $("#uploadCompletionMessage").show();
                uploadCompletionMessage.textContent  = result;
                uploadCompletionMessage.style.color = "green";
            },
            error: function (error) {
                $("#uploadCompletionMessage").show();
                uploadCompletionMessage.textContent  = error.responseText;
                uploadCompletionMessage.style.color = "red";
            }
        });
    });
    
    $("#uploadButton").click(function () {
        rFile.upload();
    });

}

function updateFile(uploadRoute, updateRoute, documentType) {

    const rFile = new Resumable({
        target: uploadRoute(documentType).url,
        fileType: getFileTypes(documentType),
        query: {csrfToken: $('[name="csrfToken"]').attr('value')}
    });

    rFile.assignBrowse(document.getElementById('browseUpdateButton'));

    rFile.assignDrop(document.getElementById('updateSelector'));

    rFile.on('fileAdded', function (file) {
        $("#updater").show();
        $("#updateSelector").hide();
        $("#fileName").html(file.fileName);
    });

    rFile.on('fileProgress', function (file) {
        moveProgressBar($("#fileUpdateProgressBar"), (file.progress(false) * 100.00));
    });

    let updateCompletionMessage = document.getElementById('updateCompletionMessage');
    rFile.on('fileSuccess', function (file) {
        $("#updateControls").delay(1000).fadeOut(1000);
        let updateDbRoute = updateRoute(file.fileName, documentType);
        $.ajax({
            url: updateDbRoute.url,
            type: updateDbRoute.type,
            success: function (result) {
                $("#updateCompletionMessage").show();
                updateCompletionMessage.textContent  = result;
                updateCompletionMessage.style.color = "green";
            },
            error: function (error) {
                $("#updateCompletionMessage").show();
                updateCompletionMessage.textContent  = error.responseText;
                updateCompletionMessage.style.color = "red";
            }
        });
    });

    $("#updateButton").click(function () {
        rFile.upload();
    });

}