window.addEventListener('popstate', (event) => {
        addState = false
        const reg = new RegExp('^[0-9]+$');

        //The last part of URL -> eg. "409925" in http://localhost:9000/blocks/409925
        var lastPart = window.location.href.substr(window.location.href.lastIndexOf('/') + 1);

        //The first last part of URL URL after domain/port name -> eg. "blocks" in http://localhost:9000/blocks/409925
        secondLastPart = window.location.href.split('/').splice(3, 3).join('/').split('/').shift();

        if (reg.test(lastPart) && secondLastPart === "blocks") {
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.block(parseInt(lastPart)))
        } else if (secondLastPart === "blocks") {
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.blockList());
        } else if (lastPart != "validators" && secondLastPart === "validators" && lastPart != "") {
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.validator(lastPart))
        } else if (secondLastPart === "validators") {
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.validatorList());
        } else if (lastPart != "transactions" && secondLastPart === "transactions" && lastPart != "") {
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.transaction(lastPart))
        } else if (secondLastPart === "transactions") {
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.transactionList());
        } else if (lastPart != "proposals" && secondLastPart === "proposals" && lastPart != "") {
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.proposal(parseInt(lastPart)))
        } else if (secondLastPart === "proposals") {
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.proposalList());
        } else if (lastPart != "wallet" && secondLastPart === "wallet" && lastPart != "" && lastPart != "") {
            console.log("Wallet second last part -> " + lastPart + "last part " + secondLastPart)
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.wallet(lastPart));
        } else {
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.dashboard());
        }
    }
)