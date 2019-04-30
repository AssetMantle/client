function getConfiguration(configuration) {
    var route = jsRoutes.controllers.ConfigurationController.queryConfigurationVariable(configuration);
    var response = null;
    $.ajax({
        url: route.url,
        type: route.type,
        async: false,
        statusCode: {
            200: function (result) {
                response= result;
            }
        }
    });
    return response;
}