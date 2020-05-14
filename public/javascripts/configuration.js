function getConfiguration(configuration) {
    let configurationValue = getCookie(configuration);
    if (configurationValue !== "") {
        return configurationValue;
    } else {
        let route = jsRoutes.controllers.ConfigurationController.queryConfigurationVariable(configuration);
        $.ajax({
            url: route.url,
            type: route.type,
            async: false,
            global: showSpinner('configuration'),
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
        $.ajax({
            url: route.url,
            type: route.type,
            async: true,
            global: showSpinner('configuration'),
            statusCode: {
                200: function (data) {
                    setCookie(configuration, data, 1);
                    return getCookie(configuration);
                }
            }
        });
    }
}