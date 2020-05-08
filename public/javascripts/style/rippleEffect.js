(function(window, $) {

    $(function() {

        $(document).on('click', 'button', function(event) {
            console.log("asdfdsa")
            var $btn = $(this),
                $div = $('<div/>'),
                btnOffset = $btn.offset(),
                xPos = event.pageX - btnOffset.left,
                yPos = event.pageY - btnOffset.top;

            $div.addClass('ripple-effect');
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

(function(window, $) {

    $(function() {

        $(document).on('click', '.navItemRippleEffect', function(event) {
            console.log("asdfdsa")
            var $btn = $(this),
                $div = $('<div/>'),
                btnOffset = $btn.offset(),
                xPos = event.pageX - btnOffset.left,
                yPos = event.pageY - btnOffset.top;

            $div.addClass('ripple-effect');
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
            }, 300);
        });

    });

})(window, jQuery);
