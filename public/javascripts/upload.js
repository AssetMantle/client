function getUserUploadJsRoutes(documentType, target) {
    let uploadRoute = '';
    switch (documentType) {
        case "BANK_DETAILS":
        case "IDENTIFICATION":
            switch (target) {
                case "ZONE":
                    uploadRoute = jsRoutes.controllers.FileController.uploadZoneKycUser(documentType);
                    break;
                case "ORGANIZATION":
                    uploadRoute = jsRoutes.controllers.FileController.uploadOrganizationKycUser(documentType);
                    break;
                default:
                    uploadRoute = jsRoutes.controllers.FileController.uploadUserKYC(documentType);
                    break;
            }
    }
    return uploadRoute
}

function getZoneUploadJsRoutes(documentType) {
    let uploadRoute = '';
    switch (documentType) {
        case "BANK_DETAILS":
        case "IDENTIFICATION":
            uploadRoute = jsRoutes.controllers.FileController.uploadZoneKYC(documentType);
            break;
    }
    return uploadRoute
}

function getOrganizationUploadJsRoutes(documentType) {
    let uploadRoute = '';
    switch (documentType) {
        case "BANK_DETAILS":
        case "IDENTIFICATION":
            uploadRoute = jsRoutes.controllers.FileController.uploadOrganizationKYC(documentType);
            break;
    }
    return uploadRoute
}

function getUserStoreAndUpdateJsRoutes(documentType, fileName, target) {
    let storeRoute = '';
    let updateRoute = '';
    switch (documentType) {
        case "BANK_DETAILS":
        case "IDENTIFICATION":
            switch (target) {
                case "ZONE":
                    updateRoute = jsRoutes.controllers.FileController.updateZoneKycUser(fileName, documentType);
                    storeRoute = jsRoutes.controllers.FileController.storeZoneKycUser(fileName, documentType);
                    break;
                case "ORGANIZATION":
                    updateRoute = jsRoutes.controllers.FileController.updateOrganizationKycUser(fileName, documentType);
                    storeRoute =  jsRoutes.controllers.FileController.storeOrganizationKycUser(fileName, documentType);
                    break;
                default:
                    updateRoute = jsRoutes.controllers.FileController.updateUserKYC(fileName, documentType);
                    storeRoute = jsRoutes.controllers.FileController.storeUserKYC(fileName, documentType);
                    break;
            }
    }
    return [storeRoute, updateRoute];
}

function getZoneStoreAndUpdateJsRoutes(documentType, fileName) {
    let storeRoute = '';
    let updateRoute = '';
    switch (documentType) {
        case "BANK_DETAILS":
        case "IDENTIFICATION":
            updateRoute = jsRoutes.controllers.FileController.updateZoneKYC(fileName, documentType);
            storeRoute = jsRoutes.controllers.FileController.storeZoneKYC(fileName, documentType);
            break;
    }
    return [storeRoute, updateRoute];
}

function getOrganizationStoreAndUpdateJsRoutes(documentType, fileName) {
    let storeRoute = '';
    let updateRoute = '';
    switch (documentType) {
        case "BANK_DETAILS":
        case "IDENTIFICATION":
            updateRoute = jsRoutes.controllers.FileController.updateOrganizationKYC(fileName, documentType);
            storeRoute = jsRoutes.controllers.FileController.storeOrganizationKYC(fileName, documentType);
            break;
    }
    return [storeRoute, updateRoute];
}

function getUploadJsRoutes(documentType, userType, target) {
    let uploadRoute = '';
    switch (userType) {
        case "ZONE":
            uploadRoute = getZoneUploadJsRoutes(documentType);
            break;
        case "ORGANIZATION":
            uploadRoute = getOrganizationUploadJsRoutes(documentType);
            break;
        case "USER":
            uploadRoute = getUserUploadJsRoutes(documentType, target);
            break;
        default:
            uploadRoute = jsRoutes.controllers.FileController.uploadAccountFile(documentType);
            break;
    }
    return uploadRoute;
}

function getStoreAndUpdateJsRoutes(documentType, userType, fileName, target) {
    let storeRoute = '';
    let updateRoute = '';
    switch (userType) {
        case "ZONE":
            [storeRoute, updateRoute] = getZoneStoreAndUpdateJsRoutes(documentType, fileName);
            break;
        case "ORGANIZATION":
            [storeRoute, updateRoute] = getOrganizationStoreAndUpdateJsRoutes(documentType, fileName);
            break;
        case "USER":
            [storeRoute, updateRoute] = getUserStoreAndUpdateJsRoutes(documentType, fileName, target);
            break;
        default:
            storeRoute = jsRoutes.controllers.FileController.storeAccountFile(fileName, documentType);
            updateRoute = jsRoutes.controllers.FileController.updateAccountFile(fileName, documentType);
            break;
    }
    return [storeRoute, updateRoute]
}

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

function uploadFile(documentType, userType, target) {
    
    const rFile = new Resumable({
        target: getUploadJsRoutes(documentType, userType, target).url,
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
        let [storeRoute, _] = getStoreAndUpdateJsRoutes(documentType, userType, file.fileName, target);
        $.ajax({
            url: storeRoute.url,
            type: storeRoute.type,
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

function updateFile(documentType, userType, target) {

    const rFile = new Resumable({
        target: getUploadJsRoutes(documentType, userType, target).url,
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
        let [_, updateRoute] = getStoreAndUpdateJsRoutes(documentType, userType, file.fileName, target);
        $.ajax({
            url: updateRoute.url,
            type: updateRoute.type,
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