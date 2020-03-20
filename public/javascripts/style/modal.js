$(document).mouseup(function (e) {
    $('.modal').each(function () {
        const modal = $(this);
        if (modal.is(e.target)) {
            // modal.fadeOut(400);
            $(modal).modal({
                backdrop: 'static',
                keyboard: false
            })
        }
        $('#modalClose').click(function() {
            modal.fadeOut();
        })
    })


});
