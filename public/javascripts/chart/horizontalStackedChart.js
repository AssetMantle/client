$('.value').each(function() {
    var text = $(this).text();
    $(this).parent().css('width', text);
});

$('.block').tooltip();