function lineChart(chartID, keys, values, label, showLegend, xLabel, yLabel) {

    let nameList = keys.replace('Set(', '').replace(')', '').split(', ');
    let valueList = values.replace('MapLike.DefaultValuesIterable(', '').replace(')', '').split(', ');

    Chart.defaults.global.legend.display = showLegend;
    let chartData = {
        labels: nameList,
        datasets: [
            {
                label: label,
                data: valueList,
                fillColor: "rgba(220,220,220,0.2)",
                borderColor: 'rgba(220,220,220,1)',
                backgroundColor: 'rgba(220,220,220,0.2)'
            }]
    };
    let ctx = $('#' + chartID);
    let chart = new Chart(ctx, {
        type: 'line',
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
                    scaleLabel: {
                        display: true,
                        labelString: yLabel
                    }
                }]
            }
        }
    });
}