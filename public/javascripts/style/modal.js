$(document).mouseup(function (e) {
    $('.modal').each(function () {
        const modal = $(this);
        const modalConnectionError = $('#connectionError');
        if (modalConnectionError.is(e.target)) {
            modalConnectionError.fadeOut(100)
        }
        $('#modalClose').click(function() {
            modal.fadeOut(100);
            $('.modalContent').removeClass('fadeInEffect');
        })
    })
});