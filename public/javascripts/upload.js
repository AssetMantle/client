function getFileTypes(documentType) {
    let fileTypes = [];
    switch (documentType) {
        case "PROFILE_PICTURE":
            fileTypes = ['jpg', 'png', 'jpeg', 'JPG', 'JPEG', 'PNG'];
            break;
        case "CONTRACT":
            fileTypes = ['pdf'];
            break;
        default:
            fileTypes = ['jpg', 'png', 'jpeg', 'JPG', 'JPEG', 'PNG', 'pdf', 'doc', 'docx'];
            break;
    }
    return fileTypes
}

function uploadFile(uploadRoute, storeRoute, documentType, id) {
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
        let storeDbRoute = storeRoute(file.fileName, documentType, id);

        $.ajax({
            url: storeDbRoute.url,
            type: storeDbRoute.type,
            statusCode: {
                200: function (data) {
                    $("#uploadCompletionMessage").show();
                    uploadCompletionMessage.textContent  = data;
                },
                206: function (data) {
                    $("#commonModalContent").html(data);
                },
                400: function (error) {
                    $("#uploadCompletionMessage").show();
                    uploadCompletionMessage.textContent  = error.responseText;
                },
                500: function (data) {
                    const newDocument = document.open("text/html", "replace");
                    newDocument.write(data.responseText);
                    newDocument.close();
                }
            }
        });
    });

    $("#uploadButton").click(function () {
        rFile.upload();
    });

}

function updateFile(uploadRoute, updateRoute, documentType, id) {
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
        let updateDbRoute = updateRoute(file.fileName, documentType, id);
        $.ajax({
            url: updateDbRoute.url,
            type: updateDbRoute.type,
            statusCode: {
                200: function (data) {
                    $("#updateCompletionMessage").show();
                    updateCompletionMessage.textContent  = data;
                },
                206: function (data) {
                    $("#commonModalContent").html(data);
                },
                400: function (error) {
                    $("#updateCompletionMessage").show();
                    updateCompletionMessage.textContent  = error.responseText;
                },
                500: function (data) {
                    const newDocument = document.open("text/html", "replace");
                    newDocument.write(data.responseText);
                    newDocument.close();
                }
            }
        });
    });

    $("#updateButton").click(function () {
        rFile.upload();
    });

}