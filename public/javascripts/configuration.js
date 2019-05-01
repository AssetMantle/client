function getConfiguration(configuration) {
    if (getCookie(configuration) !== ""){
        return getCookie(configuration)
    }else {
        let route = jsRoutes.controllers.ConfigurationController.queryConfigurationVariable(configuration);
        $.ajax({
            url: route.url,
            type: route.type,
            async: false,
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
    let route = jsRoutes.controllers.ConfigurationController.queryConfigurationVariable(configuration);
    $.ajax({
        url: route.url,
        type: route.type,
        async: true,
        statusCode: {
            200: function (data) {
                setCookie(configuration, data, 1);
                return getCookie(configuration);
            }
        }
    });
}

function getCookie(cookieName) {
    var name = cookieName + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i <ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) === ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function setCookie(cookieName, cookieValue, expiryDays) {
    var d = new Date();
    d.setTime(d.getTime() + (expiryDays*24*60*60*1000));
    var expires = "expires="+ d.toUTCString();
    document.cookie = cookieName + "=" + cookieValue + ";" + expires + ";path=/";
}