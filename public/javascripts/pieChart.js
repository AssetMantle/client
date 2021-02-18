function pieChart(chartID, keys, values, showLegend) {
    let nameList = keys.replace('Set(', '').replace(')', '').split(', ');
    let valueList = values.replace('MapLike.DefaultValuesIterable(', '').replace(')', '').split(', ');
    let totalValue = 0.0;
    let colors = [];
    for (let i = 0; i < valueList.length; i++) {
        totalValue = totalValue + parseFloat(valueList[i]);
        colors.push('#' + ((i + 1) * 16777215) / (valueList.length + 1).toString(16));
    }

    Chart.defaults.global.legend.display = showLegend;

    let chartData = {
        labels: nameList,
        datasets: [
            {
                data: valueList,
                backgroundColor: colors,
            }]
    };
    let ctx = $('#' + $.escapeSelector(chartID));
    let chart = new Chart(ctx, {
        type: 'pie',
        data: chartData,
        options: {
            tooltips: {
                displayColors: false,
                callbacks: {
                    label: function (tooltipItem, data) {
                        let name = nameList[tooltipItem.index];
                        let value = valueList[tooltipItem.index];
                        let dataset = data.datasets[tooltipItem.datasetIndex];
                        return [name, (value * 100.0 / totalValue).toFixed(2) + "%", value];
                    }
                }
            },
            responsive: true,
            maintainAspectRatio: false,
            legend: {
                position: 'right',
                align: 'center',
                labels: {
                    boxWidth: 10,
                    boxHeight: 2
                }
            }
        }
    });
}
