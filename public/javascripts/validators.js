function getValidators(urlGetValidators, bodyID){
    let listValidators =  JSON.parse(httpGet(urlGetValidators));
    document.getElementById(bodyID).innerHTML = "Validators:"+listValidators.length;
}

function validatorsTable(restIpPort, bodyID) {
    var content = "";
    let validatorsList = JSON.parse(httpGet(restIpPort + "/stake/validators"));
    Array.prototype.forEach.call(validatorsList, validator => {
        content = content + "<tr><td>" + validator["operator"] + "</td><td>" + validator["status"] + "</td><td >" + validator["tokens"] + "</div></td></td><td >" + validator["delegator_shares"] + "</td></tr>";
    });
    $("#" + bodyID).append(content);
}