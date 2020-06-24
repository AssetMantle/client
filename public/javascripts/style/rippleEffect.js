(function(window, $) {
    $(function() {
        $(document).on('click', '.cmuk-button , .navItemRippleEffect', function(event) {
            var $btn = $(this),
                $div = $('<div/>'),
                btnOffset = $btn.offset(),
                xPos = event.pageX - btnOffset.left,
                yPos = event.pageY - btnOffset.top;
            $div.addClass('rippleEffect');
            $div
                .css({
                    height: $btn.height(),
                    width: $btn.height(),
                    top: yPos - ($div.height() / 2),
                    left: xPos - ($div.width() / 2),
                    background: $btn.data("ripple-color") || "#fff"
                });
            $btn.append($div);
            window.setTimeout(function() {
                $div.remove();
            }, 200);
        });
    });
})(window, jQuery);