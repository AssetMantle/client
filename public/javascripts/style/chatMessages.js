if ($("div.chatMessages").length == 1) {
    $('#chatMessages').scrollTop($('#chatMessages')[0].scrollHeight - $('#chatMessages')[0].clientHeight)
}