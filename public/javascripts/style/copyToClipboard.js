function copyToClipboard(elementId) {
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

    var tooltip = document.getElementById("toolTip");
    tooltip.innerHTML = "Copied";

}
function afterCopy() {
    var tooltip = document.getElementById("toolTip");
    tooltip.innerHTML = "Copy to clipboard";
}