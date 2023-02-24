lastUrl = location.href;

function navBar() {
    $('nav .cmuk-navbar-nav li a').filter(function (idx, elem) {
        if ('/' + elem.getAttribute("id") === window.location.pathname) {
            $(elem).addClass('active');
        } else {
            switch (history.state) {
                case "block":
                case "blockList":
                    $('#blocks').addClass('active');
                    break;
                case "validator":
                case "validatorList":
                    $('#validators').addClass('active');
                    break;
                case "transaction":
                case "transactionList":
                    $('#transactions').addClass('active');
                    break;
                case "proposalList":
                    $('#proposals').addClass('active');
                    break;
                case "parameterList":
                    $('#parameters').addClass('active');
                    break;
                case "":
                    $('#dashBoard').addClass('active');
                    break;
                default:
                    var elems = document.querySelectorAll(".active");
                    [].forEach.call(elems, function (el) {
                        el.classList.remove("active");
                    });
            }
        }
    });
};

function setActiveLink(e) {
    var elems = document.querySelectorAll(".active");
    [].forEach.call(elems, function (el) {
        el.classList.remove("active");
    });
    e.target.className = "active";
}

$(document).ready(function () {
    checkAndPushState("", "", "");
    navBar()
})

new MutationObserver(() => {
    const url = location.href;
    if (url !== lastUrl) {
        lastUrl = url;
        onUrlChange();
    }
}).observe(document, {subtree: true, childList: true});

function onUrlChange() {
    var elems = document.querySelectorAll(".active");
    [].forEach.call(elems, function (el) {
        el.classList.remove("active");
    });
    navBar()
}


