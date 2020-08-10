function showStatistics(symbolList) {
    let symbols = symbolList.replace('Vector(', '').replace(')', '').split(', ');
    for (let i = 0; i < symbols.length; i++) {
        $('#tokenStatistics_' + symbols[i]).hide();
    }
    $('#tokenStatistics_' + $('#tokensStatisticsSymbolSelect').val()).show();
}

$(document).ready(function () {
    $('#tokenStatistics_' + $('#tokensStatisticsSymbolSelect').val()).show();
});