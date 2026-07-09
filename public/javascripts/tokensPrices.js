// function showPrices(symbolList) {
//     let symbols = symbolList.replace('Set(', '').replace(')', '').split(',');
//     for (let i = 0; i < symbols.length; i++) {
//         console.log(symbols[i]);
//         $('#tokenPrices_' + symbols[i].replace(' ', '')).hide();
//     }
//     $('#tokenPrices_' + $('#tokensPricesSymbolSelect').val()).show();
// }
//
// $(document).ready(function () {
//     $('#tokenPrices_' + $('#tokensPricesSymbolSelect').val()).show();
// });