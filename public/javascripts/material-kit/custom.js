function openNav() {
    document.getElementById("mySidenav").style.width = "200px";
}

function closeNav() {
    document.getElementById("mySidenav").style.width = "0px";
}
function modal_close()
{
    document.getElementById("commonModal").style.display = "none";
}
//
// $(document).mouseup(function (e) {
//     var popup = $("#mySidenav");
//     if (!$('#sidebar_btn').is(e.target) && !popup.is(e.target) && popup.has(e.target).length == 0) {
//         document.getElementById("mySidenav").style.width = "0px";
//     }
// });
function copyToClipboard(elementId) {

    // Create an auxiliary hidden input
    var aux = document.createElement("input");

    // Get the text from the element passed into the input
    aux.setAttribute("value", document.getElementById(elementId).innerHTML);

    // Append the aux input to the body
    document.body.appendChild(aux);

    // Highlight the content
    aux.select();

    // Execute the copy command
    document.execCommand("copy");

    // Remove the input from the body
    document.body.removeChild(aux);

}

