function getConfiguration(configuration) {
    let configurationValue = getCookie(configuration);
    if (configurationValue !== "") {
        return configurationValue;
    } else {
        let route = jsRoutes.controllers.ConfigurationController.queryConfigurationVariable(configuration);
        let loadingSpinner = $('#commonSpinner');
        $.ajax({
            url: route.url,
            type: route.type,
            async: false,
            global: showSpinner('configuration'),
            beforeSend: function () {
                loadingSpinner.show();
            },
            complete: function () {
                loadingSpinner.hide();
            },
            statusCode: {
                200: function (data) {
                    setCookie(configuration, data, 1)
                }
            }
        });
    }
    return getCookie(configuration)
}

function getConfigurationAsynchronously(configuration) {
    let configurationValue = getCookie(configuration);
    if (configurationValue !== "") {
        return configurationValue;
    } else {
        let route = jsRoutes.controllers.ConfigurationController.queryConfigurationVariable(configuration);
        let loadingSpinner = $('#commonSpinner');
        $.ajax({
            url: route.url,
            type: route.type,
            async: true,
            global: showSpinner('configuration'),
            beforeSend: function () {
                loadingSpinner.show();
            },
            complete: function () {
                loadingSpinner.hide();
            },
            statusCode: {
                200: function (data) {
                    setCookie(configuration, data, 1);
                    return getCookie(configuration);
                }
            }
        });
    }
}