function graph(title, chartID) {
    var barChartData = {
        labels: [
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        ],
        datasets: [
            {
                label: "Total Sell",
                backgroundColor: "#268CFD",
                data: [3, 5, 1, 2,3, 5, 4, 2,2,6,2,8]
            },
            {
                label: "Total Buy",
                backgroundColor: "#DDE3EA",
                data: [4, 2, 3, 2, 1,2,4,3,1,3,4,1]
            }
        ]
    };

    var chartOptions = {
        maintainAspectRatio: false,
        legend: {
            position: "bottom",
            align: "start",
            labels: {
                boxWidth: 10,
                boxHeight: 2
            }
        },
        scales: {
            yAxes: [{
                gridLines: {
                    color: '#E6E6E6',
                    borderDash: [8, 4]
                },
                ticks: {
                    stepSize: 2,
                }
            }],
            xAxes: [{
                barPercentage: 1,
                categoryPercentage: 0.5,
                gridLines: {
                    color: '#E6E6E6',
                    borderDash: [8, 4]
                }
            },
            ]
        }
    }
    let ctx = $('#' + chartID);
    let chart = new Chart(ctx, {
        type: 'bar',
        data: barChartData,
        options: chartOptions
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