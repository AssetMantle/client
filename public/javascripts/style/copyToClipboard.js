function copyToClipboard(elementId, toolTipID) {
    // Create an auxiliary hidden input
    let aux = document.createElement("input");
    // Get the text from the element passed into the input
    aux.setAttribute("value", $('#' + elementId).html());
    // Append the aux input to the body
    document.body.appendChild(aux);
    // Highlight the content
    aux.select();
    // Execute the copy command
    document.execCommand("copy");
    // Remove the input from the body
    document.body.removeChild(aux);

    var tooltip = document.getElementById(toolTipID);
    tooltip.innerHTML = "Copied";
    console.log("CopytoClipboard")

}
function afterCopy(toolTipID) {
    console.log("AfterCopy")
    var tooltip = document.getElementById(toolTipID);
    tooltip.innerHTML = "Copy to clipboard";
}
