$(document).ready(function () {
    let updatingFile = false;

    let rImage = new Resumable({
        target: jsRoutes.controllers.FileUploadController.uploadImages().url,
        fileType: ['jpg', 'png', 'jpeg'],
        query: {csrfToken: $('[name="csrfToken"]').attr('value')}
    });
    let rPDF = new Resumable({
        target: jsRoutes.controllers.FileUploadController.uploadPDFs().url,
        fileType: ['pdf'],
        query: {csrfToken: $('[name="csrfToken"]').attr('value')}
    });
    let rDOC = new Resumable({
        target: jsRoutes.controllers.FileUploadController.uploadDOCs().url,
        fileType: ['doc', 'docx', 'txt'],
        query: {csrfToken: $('[name="csrfToken"]').attr('value')}
    });
    let retries = 0;
    document.getElementById('uploadButton').onclick = function () {
        rImage.upload();
        rPDF.upload();
        rDOC.upload();
        retries = 0;
    };
    document.getElementById('updateButton').onclick = function () {
        updatingFile = true;
        rImage.upload();
        rPDF.upload();
        rDOC.upload();
        retries = 0;
    };
    document.getElementById('pauseButton').onclick = function () {
        rImage.pause();
        rPDF.pause();
        rDOC.pause();
    };
    document.getElementById('cancelButton').onclick = function () {
        rImage.cancel();
        rPDF.cancel();
        rDOC.cancel();
        retries = 0;
    };

    let errorMessage = document.getElementById('errorMessage');
    let successfulMessage = document.getElementById('successfulMessage');
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
            rImage.removeFile(file);
            rPDF.removeFile(file);
            rDOC.removeFile(file);
        }
    }

    rImage.assignBrowse(document.getElementById('browseButtonImages'));
    rImage.on('fileAdded', function (file) {
        addFileToList(file);
    });
    rImage.on('fileProgress', function (file) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = (file.progress(false) * 100.00) + '%';
    });

    rImage.on('fileSuccess', function (file) {
        let element = document.getElementById(file.uniqueIdentifier + "-progress");
        let route = "";
        if (updatingFile) {
            route = jsRoutes.controllers.FileUploadController.updateImage(file.fileName);
        } else {
            route = jsRoutes.controllers.FileUploadController.storeImage(file.fileName);
        }
        $.ajax({
            url: route.url,
            type: route.type,
            success: function (result) {
                successfulMessage.textContent = result;
            },
            error: function (error) {
                element.textContent = error.responseText;
                errorMessage.textContent = error.responseText;
            }
        });
        updatingFile = false;
    });

    rImage.on('cancel', function (file) {
        $(".uploadProgress").css("display", "flex");
        let anchors = filesSpace.getElementsByTagName('a');
        for (let i = anchors.length - 1; i >= 0; i--) {
            anchors[i].click();
        }
        errorMessage.textContent = 'Upload canceled';
    });

    rImage.on('progress', function () {
        $(".uploadProgress").css("display", "flex");
        progress.textContent = (rImage.progress() * 100.00);
        let percent = progress.textContent;
        document.querySelector(".progressBar").style.width = percent + "%";
        document.querySelector(".progressBar").textContent = percent.toFixed(0) + "%";
    });
    rImage.on('fileError', function (file, msg) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = msg;
        errorMessage.textContent = msg;
    });
    rImage.on('fileRetry', function (file) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = 'Retrying upload';
        retries++;
        errorMessage.textContent = "Retried " + retries + "time(s)";
        if (retries > 10) {
            rImage.pause();
            errorMessage.textContent = 'Pausing file upload after ' + retries + ' attempts';
        }
    });
    rImage.on('error', function (message, file) {
        errorMessage.textContent = message;
    });
    rImage.on('catchAll', function (eventX) {
        console.log(eventX)
    });


    rPDF.assignBrowse(document.getElementById('browseButtonPDFs'));
    rPDF.on('fileAdded', function (file) {
        addFileToList(file);
    });
    rPDF.on('fileProgress', function (file) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = (file.progress(false) * 100.00) + '%';
    });
    rPDF.on('fileSuccess', function (file) {
        let element = document.getElementById(file.uniqueIdentifier + "-progress");
        let route = "";
        if (updatingFile) {
            route = jsRoutes.controllers.FileUploadController.updatePDF(file.fileName);
        } else {
            route = jsRoutes.controllers.FileUploadController.storePDF(file.fileName);
        }
        $.ajax({
            url: route.url,
            type: route.type,
            success: function (result) {
                successfulMessage.textContent = result;
            },
            error: function (error) {
                element.textContent = error.responseText;
                errorMessage.textContent = error.responseText;
            }
        });
        updatingFile = false;
    });
    rPDF.on('cancel', function (file) {
        $(".uploadProgress").css("display", "flex");
        let anchors = filesSpace.getElementsByTagName('a');
        for (let i = anchors.length - 1; i >= 0; i--) {
            anchors[i].click();
        }
        errorMessage.textContent = 'Upload canceled';
    });
    rPDF.on('progress', function () {
        $(".uploadProgress").css("display", "flex");
        progress.textContent = (rImage.progress() * 100.00);
        let percent = progress.textContent;
        document.querySelector(".progressBar").style.width = percent + "%";
        document.querySelector(".progressBar").textContent = percent.toFixed(0) + "%";
    });
    rPDF.on('fileError', function (file, msg) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = msg;
        errorMessage.textContent = msg;
    });
    rPDF.on('fileRetry', function (file) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = 'Retrying upload';
        retries++;
        errorMessage.textContent = "Retried " + retries + "time(s)";
        if (retries > 10) {
            rPDF.pause();
            errorMessage.textContent = 'Pausing file upload after ' + retries + ' attempts';
        }
    });
    rPDF.on('error', function (message, file) {
        errorMessage.textContent = message;
    });
    rPDF.on('catchAll', function (eventX) {
    });


    rDOC.assignBrowse(document.getElementById('browseButtonDOCs'));
    rDOC.on('fileAdded', function (file) {
        addFileToList(file);
    });
    rDOC.on('fileProgress', function (file) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = (file.progress(false) * 100.00) + '%';
    });
    rDOC.on('fileSuccess', function (file) {
        let element = document.getElementById(file.uniqueIdentifier + "-progress");
        let route = "";
        if (updatingFile) {
            route = jsRoutes.controllers.FileUploadController.updateDOC(file.fileName);
        } else {
            route = jsRoutes.controllers.FileUploadController.storeDOC(file.fileName);
        }
        $.ajax({
            url: route.url,
            type: route.type,
            success: function (result) {
                successfulMessage.textContent = result;
            },
            error: function (error) {
                element.textContent = error.responseText;
                errorMessage.textContent = error.responseText;
            }
        });
        updatingFile = false;
    });
    rDOC.on('cancel', function (file) {
        $(".uploadProgress").css("display", "flex");
        let anchors = filesSpace.getElementsByTagName('a');
        for (let i = anchors.length - 1; i >= 0; i--) {
            anchors[i].click();
        }
        errorMessage.textContent = 'Upload canceled';
    });
    rDOC.on('progress', function () {
        $(".uploadProgress").css("display", "flex");
        progress.textContent = (rImage.progress() * 100.00);
        let percent = progress.textContent;
        document.querySelector(".progressBar").style.width = percent + "%";
        document.querySelector(".progressBar").textContent = percent.toFixed(0) + "%";
    });
    rDOC.on('fileError', function (file, msg) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = msg;
        errorMessage.textContent = msg;
    });
    rDOC.on('fileRetry', function (file) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = 'Retrying upload';
        retries++;
        errorMessage.textContent = "Retried " + retries + "time(s)";
        if (retries > 10) {
            rDOC.pause();
            errorMessage.textContent = 'Pausing file upload after ' + retries + ' attempts';
        }
    });
    rDOC.on('error', function (message, file) {
        errorMessage.textContent = message;
    });
    rDOC.on('catchAll', function (eventX) {
    });
});

