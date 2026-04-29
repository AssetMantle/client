function lineChart(chartID, keys, values, label, showLegend, xLabel, yLabel) {

    let nameList = keys.replace('List(', '').replace(')', '').split(', ');
    let valueList = values.replace('Iterable(', '').replace(')', '').split(', ');

    function formatYValue(value) {
        if (yLabel !== 'USD') return value;
        let v = Number(value);
        if (!isFinite(v)) return value;
        if (v === 0) return '$0';
        let abs = Math.abs(v);
        if (abs >= 1) return '$' + v.toLocaleString('en-US', {maximumFractionDigits: 2});
        if (abs >= 0.01) return '$' + v.toFixed(4);
        let decimals = Math.min(12, Math.max(4, -Math.floor(Math.log10(abs)) + 3));
        return '$' + v.toFixed(decimals);
    }

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
                    },
                    ticks: {
                        callback: function (value) {
                            return formatYValue(value);
                        }
                    }
                }]
            },
            tooltips: {
                callbacks: {
                    label: function (item) {
                        let prefix = label ? label + ': ' : '';
                        return prefix + formatYValue(item.yLabel);
                    }
                }
            }
        }
    });
}
