function pieChart(chartID, keys, values, showLegend) {
    let nameList = keys.replace('Set(', '').replace(')', '').split(', ');
    let valueList = values.replace('MapLike.DefaultValuesIterable(', '').replace(')', '').split(', ');
    let totalValue = 0.0;
    let colors = [];
    let colorPrefixes = ["e", 4 , 1 , 2, 6, "b", 9, 3, "f", 5, 6, 0 , "e", 2, 8, "b", 9, 3, "f", 5, "a" , 7 , "c", 2, 6, "b", 9, 3, "f", 5,"e", 4 , 1 , 2, 6, "b", 9, 3, "f", 5, 6, 0 , "e", 2, 8, "b", 9, 3, "f", 5];
    for (let i = 0; i < valueList.length; i++) {
        totalValue = totalValue + parseFloat(valueList[i]);
        if(colorPrefixes.length <= i){
            colors.push('#' +colorPrefixes[i - colorPrefixes.length] + ((i + 50) * 884).toString());
        }else {
            colors.push('#' +colorPrefixes[i] + ((i + 50) * 884).toString());
        }
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
    let ctx = $('#' + chartID);
    let chart = new Chart(ctx, {
        type: 'doughnut',
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