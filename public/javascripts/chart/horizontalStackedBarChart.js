function horizontalStackedBarChart(chartID, keys, values, showLegend) {

    let nameList = keys.replace('List(', '').replace(')', '').split(', ');
    let valueList = values.replace('Iterable(', '').replace(')', '').split(', ');
    let totalValue = parseFloat(valueList[0]);
    let colors = [theme_colors.primary];
    let colorPrefixes = ["e", 4, 1, 2, 6, "b", 9, 3, "f", 5, 6, 0, "e", 2, 8, "b", 9, 3, "f", 5, "a", 7, "c", 2, 6, "b", 9, 3, "f", 5, "e", 4, 1, 2, 6, "b", 9, 3, "f", 5, 6, 0, "e", 2, 8, "b", 9, 3, "f", 5];
    let dataSets = [];
    for (let i = 1; i < valueList.length; i++) {
        totalValue = totalValue + parseFloat(valueList[i]);
        if (colorPrefixes.length <= i) {
            colors.push('#' + colorPrefixes[i - colorPrefixes.length] + ((i + 50) * 884).toString());
        } else {
            colors.push('#' + colorPrefixes[i] + ((i + 50) * 884).toString());
        }
    }
    for (let i = 0; i < valueList.length; i++) {
        let value = valueList[i] * 100.0 / totalValue;
        dataSets.push({data: [value], backgroundColor: colors[i]})
    }
    console.log(dataSets)
    let ctx = $('#' + chartID);
    let chart = new Chart(ctx, {
        type: 'horizontalBar',
        data: {
            labels: nameList,
            datasets: dataSets
        },
        options: {
            legend: {
                display: true,
                position: 'top',
                fullSize: false,
            },
            tooltips: {
                enabled: false
            },
            responsive: false,
            scales: {
                xAxes: [{
                    display: false,
                    stacked: true
                }],
                yAxes: [{
                    display: false,
                    stacked: true
                }],
            }
        }
    });
}