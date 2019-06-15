function getUserUploadJsRoutes(documentType, submitTo) {
    let uploadRoute = '';
    switch (documentType) {
        case "BANK_DETAILS":
        case "IDENTIFICATION":
            switch (submitTo) {
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

function getUserStoreAndUpdateJsRoutes(documentType, fileName, submitTo) {
    let storeRoute = '';
    let updateRoute = '';
    switch (documentType) {
        case "BANK_DETAILS":
        case "IDENTIFICATION":
            switch (submitTo) {
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

function getUploadJsRoutes(documentType, userType, submitTo) {
    let uploadRoute = '';
    switch (userType) {
        case "ZONE":
            uploadRoute = getZoneUploadJsRoutes(documentType);
            break;
        case "ORGANIZATION":
            uploadRoute = getOrganizationUploadJsRoutes(documentType);
            break;
        case "USER":
            uploadRoute = getUserUploadJsRoutes(documentType, submitTo);
            break;
    }
    return uploadRoute;
}

function getStoreAndUpdateJsRoutes(documentType, userType, fileName, submitTo) {
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
            [storeRoute, updateRoute] = getUserStoreAndUpdateJsRoutes(documentType, fileName, submitTo);
            break;
    }
    return [storeRoute, updateRoute]
}

function uploadFile(documentType, userType, submitTo = '') {

    let uploadRoute = getUploadJsRoutes(documentType, userType, submitTo);
    let updatingFile = false;

    let rFile = new Resumable({
        target: uploadRoute.url,
        fileType: ['jpg', 'png', 'jpeg', 'pdf', 'doc', 'txt', 'docx'],
        query: {csrfToken: $('[name="csrfToken"]').attr('value')}
    });
    let retries = 0;
    document.getElementById('uploadButton').onclick = function () {
        rFile.upload();
        retries = 0;
    };
    document.getElementById('updateButton').onclick = function () {
        updatingFile = true;
        rFile.upload();
        retries = 0;
    };
    document.getElementById('pauseButton').onclick = function () {
        rFile.pause();
    };
    document.getElementById('cancelButton').onclick = function () {
        rFile.cancel();
        retries = 0;
    };

    let uploadFailureSuccessMessage = document.getElementById('uploadFailureSuccessMessage');
    uploadFailureSuccessMessage.style.color = "red";
    let progress = document.getElementsByClassName('progressBar');
    let filesSpace = document.getElementById('filesToBeUploaded');

    function addFileToList(file) {
        if ($("#filesToBeUploaded li").length === 0) {
            let li = document.createElement('li');

            let progressBar = document.createElement('span');
            progressBar.textContent = '0 %';
            progressBar.id = file.uniqueIdentifier + "-progress";

            let fileNameSpan = document.createElement('span');
            fileNameSpan.textContent = file.fileName;

            let cancelButton = document.createElement('a');
            cancelButton.href = '#';
            cancelButton.textContent = 'Cancel';
            cancelButton.onclick = function () {
                file.cancel();
                filesSpace.removeChild(li);
            };

            li.setAttribute('style', 'border: solid black thin; border-radius : 5px; margin-top : 10px; padding : 5px 10px');
            li.appendChild(fileNameSpan);
            li.appendChild(document.createElement('br'));
            li.appendChild(progressBar);
            li.appendChild(document.createElement('br'));
            li.appendChild(cancelButton);

            filesSpace.appendChild(li);
        } else {
            rFile.removeFile(file);
        }
    }

    rFile.assignBrowse(document.getElementById('browseButton'));
    rFile.on('fileAdded', function (file) {
        addFileToList(file);
    });
    rFile.on('fileProgress', function (file) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = (file.progress(false) * 100.00) + '%';
    });

    rFile.on('fileSuccess', function (file) {
        let element = document.getElementById(file.uniqueIdentifier + "-progress");
        let [storeRoute, updateRoute] = getStoreAndUpdateJsRoutes(documentType, userType, file.fileName, submitTo);      
        let route = "";
        if (updatingFile) {
            route = updateRoute;
        } else {
            route = storeRoute;
        }
        $.ajax({
            url: route.url,
            type: route.type,
            success: function (result) {
                uploadFailureSuccessMessage.textContent = result;
                uploadFailureSuccessMessage.style.color = "green";
            },
            error: function (error) {
                element.textContent = error.responseText;
                uploadFailureSuccessMessage.textContent = error.responseText;
                uploadFailureSuccessMessage.style.color = "red";
            }
        });
        updatingFile = false;
    });

    rFile.on('cancel', function (file) {
        $(".uploadProgress").css("display", "flex");
        let anchors = filesSpace.getElementsByTagName('a');
        for (let i = anchors.length - 1; i >= 0; i--) {
            anchors[i].click();
        }
        uploadFailureSuccessMessage.textContent = 'Upload canceled';
    });

    rFile.on('progress', function () {
        $(".uploadProgress").css("display", "flex");
        progress.textContent = (rFile.progress() * 100.00);
        let percent = progress.textContent;
        document.querySelector(".progressBar").style.width = percent + "%";
        document.querySelector(".progressBar").textContent = percent.toFixed(0) + "%";
    });
    rFile.on('fileError', function (file, msg) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = msg;
        uploadFailureSuccessMessage.textContent = msg;
    });
    rFile.on('fileRetry', function (file) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = 'Retrying upload';
        retries++;
        uploadFailureSuccessMessage.textContent = "Retried " + retries + "time(s)";
        if (retries > 10) {
            rFile.pause();
            uploadFailureSuccessMessage.textContent = 'Pausing file upload after ' + retries + ' attempts';
        }
    });
    rFile.on('error', function (message, file) {
        uploadFailureSuccessMessage.textContent = message;
    });
    rFile.on('catchAll', function (eventX) {
        console.log(eventX)
    });

}