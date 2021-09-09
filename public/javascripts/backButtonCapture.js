window.addEventListener('popstate', e => {
    addState = false
    //The last part of URL -> eg. "409925" in http://localhost:9000/blocks/409925
    var lastPart = window.location.href.substr(window.location.href.lastIndexOf('/') + 1);

    switch (this.history.state) {
        case "block":
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.block(parseInt(lastPart)))
            break;
        case "blockList":
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.blockList());
            break;
        case "validator":
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.validator(lastPart))
            break;
        case "validatorList":
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.validatorList());
            break;
        case "transaction":
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.transaction(lastPart))
            break;
        case "transactionList":
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.transactionList());
            break;
        case "proposal":
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.proposal(parseInt(lastPart)))
            break;
        case "proposalList":
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.proposalList());
            break;
        case "wallet":
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.wallet(lastPart));
            break;
        default:
            componentResource('explorerContent', jsRoutes.controllers.ComponentViewController.dashboard());
            break;
    }
})

