$(document).mouseup(function (e) {
    $('.modal').each(function () {
        const modal = $(this);
        const modalConnectionError = $('#connectionError');
        if (modalConnectionError.is(e.target)) {
            modalConnectionError.fadeOut(200)
        }
        $('#modalClose').click(function() {
            modal.fadeOut(200);
            $('.modalContent').removeClass('fadeInEffect');
        })
    })
});