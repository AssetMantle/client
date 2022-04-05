function barChart(chartID, keys, values, label, showLegend, xLabel, yLabel) {

    let nameList = keys.replace('Set(', '').replace(')', '').split(', ');
    let valueList = values.replace('MapLike.DefaultValuesIterable(', '').replace(')', '').split(', ');

    Chart.defaults.global.legend.display = showLegend;
    let chartData = {
        labels: nameList,
        datasets: [
            {
                label: label,
                data: valueList,
                fillColor: theme_colors.primary,
                borderColor: theme_colors.primary,
                backgroundColor: theme_colors.primary
            }]
    };
    let ctx = $('#' + chartID);
    let chart = new Chart(ctx, {
        type: 'bar',
        data: chartData,
        options: {
            maintainAspectRatio: false,
            scales: {
                xAxes: [{
                    scaleLabel: {
                        display: true,
                        labelString: xLabel
                    }
                }],
                yAxes: [{
                    display: true,
                    ticks: {
                        suggestedMin: 0,
                    },
                    scaleLabel: {
                        display: true,
                        labelString: yLabel
                    }
                }]
            }
        }
    });
}