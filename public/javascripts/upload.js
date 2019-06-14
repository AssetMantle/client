function userKycFileUpload(documentType) {
    let updatingFile = false;

    let rFile = new Resumable({
        target: jsRoutes.controllers.FileController.uploadUserKYC(documentType).url,
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
        let route = "";
        if (updatingFile) {
            route = jsRoutes.controllers.FileController.updateUserKYC(file.fileName, documentType);
        } else {
            route = jsRoutes.controllers.FileController.storeUserKYC(file.fileName, documentType);
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

    rFile.on('cancel', function (file) {
        $(".uploadProgress").css("display", "flex");
        let anchors = filesSpace.getElementsByTagName('a');
        for (let i = anchors.length - 1; i >= 0; i--) {
            anchors[i].click();
        }
        errorMessage.textContent = 'Upload canceled';
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
        errorMessage.textContent = msg;
    });
    rFile.on('fileRetry', function (file) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = 'Retrying upload';
        retries++;
        errorMessage.textContent = "Retried " + retries + "time(s)";
        if (retries > 10) {
            rFile.pause();
            errorMessage.textContent = 'Pausing file upload after ' + retries + ' attempts';
        }
    });
    rFile.on('error', function (message, file) {
        errorMessage.textContent = message;
    });
    rFile.on('catchAll', function (eventX) {
        console.log(eventX)
    });
}

function userZoneKycFileUpload(documentType) {
    let updatingFile = false;

    let rFile = new Resumable({
        target: jsRoutes.controllers.FileController.uploadZoneKycUser(documentType).url,
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
        let route = "";
        if (updatingFile) {
            route = jsRoutes.controllers.FileController.updateZoneKycUser(file.fileName, documentType);
        } else {
            route = jsRoutes.controllers.FileController.storeZoneKycUser(file.fileName, documentType);
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

    rFile.on('cancel', function (file) {
        $(".uploadProgress").css("display", "flex");
        let anchors = filesSpace.getElementsByTagName('a');
        for (let i = anchors.length - 1; i >= 0; i--) {
            anchors[i].click();
        }
        errorMessage.textContent = 'Upload canceled';
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
        errorMessage.textContent = msg;
    });
    rFile.on('fileRetry', function (file) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = 'Retrying upload';
        retries++;
        errorMessage.textContent = "Retried " + retries + "time(s)";
        if (retries > 10) {
            rFile.pause();
            errorMessage.textContent = 'Pausing file upload after ' + retries + ' attempts';
        }
    });
    rFile.on('error', function (message, file) {
        errorMessage.textContent = message;
    });
    rFile.on('catchAll', function (eventX) {
        console.log(eventX)
    });
}

function userOrganiztionKycFileUpload(documentType) {
    let updatingFile = false;

    let rFile = new Resumable({
        target: jsRoutes.controllers.FileController.uploadOrganizationKycUser(documentType).url,
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
        let route = "";
        if (updatingFile) {
            route = jsRoutes.controllers.FileController.updateOrganizationKycUser(file.fileName, documentType);
        } else {
            route = jsRoutes.controllers.FileController.storeOrganizationKycUser(file.fileName, documentType);
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

    rFile.on('cancel', function (file) {
        $(".uploadProgress").css("display", "flex");
        let anchors = filesSpace.getElementsByTagName('a');
        for (let i = anchors.length - 1; i >= 0; i--) {
            anchors[i].click();
        }
        errorMessage.textContent = 'Upload canceled';
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
        errorMessage.textContent = msg;
    });
    rFile.on('fileRetry', function (file) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = 'Retrying upload';
        retries++;
        errorMessage.textContent = "Retried " + retries + "time(s)";
        if (retries > 10) {
            rFile.pause();
            errorMessage.textContent = 'Pausing file upload after ' + retries + ' attempts';
        }
    });
    rFile.on('error', function (message, file) {
        errorMessage.textContent = message;
    });
    rFile.on('catchAll', function (eventX) {
        console.log(eventX)
    });
}

function zoneKycFileUpload(documentType) {
    let updatingFile = false;

    let rFile = new Resumable({
        target: jsRoutes.controllers.FileController.uploadZoneKYC(documentType).url,
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
        let route = "";
        if (updatingFile) {
            route = jsRoutes.controllers.FileController.updateZoneKYC(file.fileName, documentType);
        } else {
            route = jsRoutes.controllers.FileController.storeZoneKYC(file.fileName, documentType);
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

    rFile.on('cancel', function (file) {
        $(".uploadProgress").css("display", "flex");
        let anchors = filesSpace.getElementsByTagName('a');
        for (let i = anchors.length - 1; i >= 0; i--) {
            anchors[i].click();
        }
        errorMessage.textContent = 'Upload canceled';
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
        errorMessage.textContent = msg;
    });
    rFile.on('fileRetry', function (file) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = 'Retrying upload';
        retries++;
        errorMessage.textContent = "Retried " + retries + "time(s)";
        if (retries > 10) {
            rFile.pause();
            errorMessage.textContent = 'Pausing file upload after ' + retries + ' attempts';
        }
    });
    rFile.on('error', function (message, file) {
        errorMessage.textContent = message;
    });
    rFile.on('catchAll', function (eventX) {
        console.log(eventX)
    });
}

function organizationKycFileUpload(documentType) {
    let updatingFile = false;

    let rFile = new Resumable({
        target: jsRoutes.controllers.FileController.uploadOrganizationKYC(documentType).url,
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
        let route = "";
        if (updatingFile) {
            route = jsRoutes.controllers.FileController.updateOrganizationKYC(file.fileName, documentType);
        } else {
            route = jsRoutes.controllers.FileController.storeOrganizationKYC(file.fileName, documentType);
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

    rFile.on('cancel', function (file) {
        $(".uploadProgress").css("display", "flex");
        let anchors = filesSpace.getElementsByTagName('a');
        for (let i = anchors.length - 1; i >= 0; i--) {
            anchors[i].click();
        }
        errorMessage.textContent = 'Upload canceled';
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
        errorMessage.textContent = msg;
    });
    rFile.on('fileRetry', function (file) {
        document.getElementById(file.uniqueIdentifier + "-progress").textContent = 'Retrying upload';
        retries++;
        errorMessage.textContent = "Retried " + retries + "time(s)";
        if (retries > 10) {
            rFile.pause();
            errorMessage.textContent = 'Pausing file upload after ' + retries + ' attempts';
        }
    });
    rFile.on('error', function (message, file) {
        errorMessage.textContent = message;
    });
    rFile.on('catchAll', function (eventX) {
        console.log(eventX)
    });
}