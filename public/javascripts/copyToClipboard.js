function copyMessageToClipboard(e) {
    var copyText = $(e).prevAll('.clipboardData').attr("data-value");
    navigator.clipboard.writeText(copyText);
}