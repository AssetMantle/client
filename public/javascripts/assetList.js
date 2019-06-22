function displayAssets(assetPegWallet, pegHash, documentHash, assetType, assetQuantity, assetPrice, quantityUnit, unmoderated, locked, redeemAsset, sendAsset, redeemAssetMessage, sendAssetMessage) {
    let content = '';
    let gta = "gta";
    let tga = "tga";
    assetPegWallet.forEach(function (asset, index) {
        content += `<div class='flexItem box assetBackgroundColor'><div class='leftAlign'>
                            ` + pegHash + ":" + asset.pegHash + `<br>
                            ` + documentHash + ":" + asset.documentHash + `<br>
                            ` + assetType + ":" + asset.assetType + `<br>
                            ` + assetQuantity + ":" + asset.assetQuantity + `<br>
                            ` + assetPrice + ":" + asset.assetPrice + `<br>
                            ` + quantityUnit + ":" + asset.quantityUnit + `<br>
                            ` + unmoderated + ":" + asset.unmoderated + `<br>
                            ` + locked + ":" + asset.locked + `<br>
                            </div>`;

        if(redeemAsset) {
           content += "<div class='centerText'><button class='width' onclick='getForm(jsRoutes.controllers.RedeemAssetController.redeemAssetForm())'>" + redeemAssetMessage + "</button><br>"
        }
        if(sendAsset) {
            content += "<button class='width' onclick='getForm(jsRoutes.controllers.SendAssetController.sendAssetForm())'>" + sendAssetMessage + "</button> </div> </div>"
        }
    });
    return content;
}