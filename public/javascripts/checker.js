function pushStateChecker(part1, part2) {
    console.log(part1, part2);
    if (addState === true) {
        if (part1 == "" && part2 == "") {
            console.log("dashboard")
            window.history.pushState("persistence", "persistence", "http://localhost:9000/");
        } else {
            let address = "/" + part1 + "/" + part2.toString()
            window.history.pushState("persistence", "persistence", address);
            console.log("THIS IS ADDED   " + address)
        }
    } else {
        console.log("back state")
        addState = true
    }

}