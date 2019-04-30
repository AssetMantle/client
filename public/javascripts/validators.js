let urlGetValidators = getConfiguration("blockchain.main.ip") + ":" + getConfiguration("blockchain.main.restPort") + "/stake/validators";

function getValidators(bodyID){
    let listValidators =  JSON.parse(httpGet(urlGetValidators));
    document.getElementById(bodyID).innerHTML = "Validators:"+listValidators.length;
}

function validatorsTable(bodyID) {
    var content = "";
    let validatorList = JSON.parse(httpGet(urlGetValidators));
    Array.prototype.forEach.call(validatorList, validator => {
        content = content + "<tr><td>" + validator["operator"] + "</td><td>" + validator["status"] + "</td><td >" + validator["tokens"] + "</div></td></td><td >" + validator["delegator_shares"] + "</td></tr>";
    });
    $("#" + bodyID).append(content);
}