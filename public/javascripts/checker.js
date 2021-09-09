function checkAndPushState(route, parameter, functionName) {
    if (addState === true) {
        if (route === "" && parameter === "") {
            window.history.pushState(functionName, "persistence", "/");
        } else {
            address = "/" + route.split('/')[1] + "/" + parameter.toString()
            window.history.pushState(functionName, "persistence", address);
        }
    } else {
        addState = true
    }

}