function showStatistics(denomList) {
    let denoms = denomList.replace('Vector(', '').replace(')', '').split(', ');
    for (let i = 0; i < denoms.length; i++) {
        $('#tokenStatistics_' + denoms[i]).hide();
    }
    $('#tokenStatistics_' + $('#tokensStatisticsDenomSelect').val()).show();
}

$(document).ready(function () {
    $('#tokenStatistics_' + $('#tokensStatisticsDenomSelect').val()).show();
});