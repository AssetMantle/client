function graph(title, chartID) {
    let ctx = $('#' + chartID);
    let chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Block Time',
                data: []
            }]
        },
        options: {
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero: true
                    },
                }]
            }
        }
    });
    ctx.data(chartID, chart);
}

function updateGraph(chartID, label, data, maxNumberOfPoints = 10) {
    let chart = $('#' + chartID).data(chartID);
    if (chart.data.datasets[0].data.length > maxNumberOfPoints) {
        chart.data.labels.splice(0, 1);
        chart.data.datasets[0].data.splice(0, 1);
    }
    for (let i = 0; i < data.length; i++) {
        chart.data.labels.push(label[i]);
        chart.data.datasets[0].data.push(data[i]);
    }
    chart.update();
}

// https://www.chartjs.org/docs/latest/

function lineChart(chartID) {

    var barChartData = {
        labels: [
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Oct",
            "Nov",
            "Dec"
        ],
        datasets: [
            {
                label: "Buy",
                backgroundColor: "#3348C0",
                borderWidth: 1,
                data: [60,66,36,76,34, 50, 36, 87,43, 58, 56, 76]
            },
            {
                label: "Sell",
                backgroundColor: "#DDE2EA",
                borderWidth: 1,
                data: [20,70,40,60,40, 70, 37, 67, 77,71,74,69]
            }
        ]
    };

    let ctx = $('#' + chartID);
    let chart = new Chart(ctx, {
        type: 'bar',
        data: barChartData,
        options: {
            maintainAspectRatio:false,
            scales: {
                xAxes: [{
                    categoryPercentage: 0.7,
                    barPercentage: 0.5,
                    barThickness: 18,
                    gridLines: {
                        color: '#E6E6E6',
                        borderDash: [8, 4]
                    }
                }],
                yAxes: [{
                    ticks: {
                        beginAtZero: true,
                        stepSize: 30,
                    }
                }]
            },
            legend: {
                // position: "bottom",
                // align: "middle",
                labels: {
                    boxWidth: 15,
                    boxHeight: 2
                }
            }

        }
    });

}
