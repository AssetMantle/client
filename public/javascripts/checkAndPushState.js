function checkAndPushState(route, parameter, functionName) {
    if (addState === true) {
        if (route === "" && parameter === "") {
            window.history.pushState(functionName, "assetMantle", "/");
        } else {
            let address = "";
            if (parameter !== "") {
                address = "/" + route.split('/')[1] + "/" + parameter.toString()
            } else {
                address = "/" + route.split('/')[1]
            }
            window.history.pushState(functionName, "assetMantle", address);
        }
    } else {
        addState = true
    }
}