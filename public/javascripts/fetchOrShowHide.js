function fetchOrShowHide(source, route, e){
    const div = $('#'+ source);
    if( !$.trim( div.html() ).length ) {
        componentResource(source, route);
        showHide($(e));
        // $(e).parent().next().slideToggle("fast");
    }
}