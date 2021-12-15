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

    let ctx = $('#' + chartID);
    let percent = 0.0;
    let slices = valueList.sort(function(a, b){return b - a}).map((v, i) => ({ label: nameList[i], value: v }))
        .reduce((accumulator, currObj) => {
             percent += 100 * currObj.value / totalValue;
            if (percent > 67) {
                const others = accumulator.find(o => o.label == 'Others');
                if (!others) {
                    return accumulator.concat({ label: 'Others', value: currObj.value });
                }
                others.value = parseFloat(others.value) + parseFloat(currObj.value);
            } else {
                accumulator.push(currObj);
            }
            return accumulator;
        }, []);
   let chart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: slices.map(o => o.label),
            datasets: [{
                data: slices.map(o => o.value),
                backgroundColor: colors,
            }]
        },
        options: {
            tooltips: {
                displayColors: false,
                callbacks: {
                    label: function (tooltipItem, data) {
                        let name = data.labels[tooltipItem.index];
                        let value = data.datasets[0].data[tooltipItem.index];
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
            },
            hover: {
                onHover: function(e, el) {
                    $("#myChart").css("cursor", e[0] ? "pointer" : "default");
                }
            }
        }
    });
}



