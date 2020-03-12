function updateTradeTermStatus(tradeID) {

    $(".cmuk-checkbox.tradeTerm").each(function () {

        $(this).click(function () {
            const updateTermsRoute = jsRoutes.controllers.TradeRoomController.updateTermStatus(tradeID,this.id,this.checked);

            $.ajax({
                url: updateTermsRoute.url,
                type: updateTermsRoute.type,
                async: true,
                statusCode: {
                    200: function (data) {
                    },
                    204: function (data) {
                    },
                    401: function (data) {
                    }
                }
            });
        })
    })

}