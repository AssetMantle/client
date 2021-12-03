function getExplorerContent(jsRoute, elementID) {
    let route = jsRoute($('#' + elementID).text().replace(/^\s+|\s+$/g, ''));
    componentResource('explorerContent', route);
}