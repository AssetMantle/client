$(".cmuk-alert-warning a").click(function (event) {
    event.stopPropagation();
    this.parentNode.parentNode.removeChild(this.parentNode);
});