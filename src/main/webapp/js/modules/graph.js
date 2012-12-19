var app = app || {};

app.graph = function() {

  var init = function() {
    initReportGraph();
  };

  var initReportGraph = function() {

    if ($('#graph').length) {
      app.loadPlugin(true, app.plugins.jquery.d3, drawReportGraph);
    }
  };

  var drawReportGraph = function() {

    var graph = d3.select("#graph");

    var margin = {top: 20, right: 20, bottom: 50, left: 40},
        width = 900 - margin.left - margin.right,
        height = 450 - margin.top - margin.bottom,
        colorSuccess = "#367D8F",
        colorFailure = "#1E454F";

    var x0 = d3.scale.ordinal()
      .rangeRoundBands([0, width], .1);

    var x1 = d3.scale.ordinal();

    var y = d3.scale.linear()
      .range([height, 0]);

    var xAxis = d3.svg.axis()
      .scale(x0)
      .orient("bottom");

    var yAxis = d3.svg.axis()
      .scale(y)
      .tickSize(-width, 0)
      .tickFormat(d3.format("d"))
      .orient("left");

    var outer = graph.append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom);

    var svg = outer.append("g")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var spinner = svg.append("image")
      .attr("x", width / 2 - 100)
      .attr("y", height / 2 - 100)
      .attr("width", 150)
      .attr("height", 150)
      .attr("xlink:href", graph.attr("data-spinner"));

    var sucLegend = outer.append("g")
      .attr("class", "legend")
      .attr("transform", function(d, i) { return "translate(" + (+margin.left - 100) + ", 425)"; });

    var failLegend = outer.append("g")
      .attr("class", "legend")
      .attr("transform", function(d, i) { return "translate(" + margin.left + ", 425)"; });

    sucLegend.append("rect")
      .attr("x", width - 18)
      .attr("width", 18)
      .attr("height", 18)
      .style("fill", colorSuccess);

    failLegend.append("rect")
      .attr("x", width - 18)
      .attr("width", 18)
      .attr("height", 18)
      .style("fill", colorFailure);

    sucLegend.append("text")
      .attr("x", width - 24)
      .attr("y", 9)
      .attr("dy", ".35em")
      .style("text-anchor", "end")
      .text("Success/NSI");

    failLegend.append("text")
      .attr("x", width - 24)
      .attr("y", 9)
      .attr("dy", ".35em")
      .style("text-anchor", "end")
      .text("Failure/GUI");
    d3.csv(graph.attr("data-url"), function(error, data) {
      if (error) {
        alert("Request resulted in " + error.status);
        return;
      }
      spinner.remove();

      var catNames = d3.keys(data[0]).filter(function(key) { return key != "Month" && !key.match(/_f$/); } );

      data.forEach(function(d) {
        d.cats = catNames.filter(function(name) { return !name.match(/_f$/); } ).map(function(name) {
          return {name: name, success: +d[name], failure: +d[name + "_f"]};
        });
      });

      x0.domain(data.map(function(d) { return d.Month; }));
      x1.domain(catNames).rangeRoundBands([0, x0.rangeBand()], .1);
      y.domain([0, d3.max(data, function(d) { return d3.max(d.cats, function(d) { return d.failure + d.success; }); })]);

      svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

      svg.append("g")
        .attr("class", "y axis")
        .call(yAxis);

      var month = svg.selectAll(".month")
        .data(data)
        .enter().append("g")
        .attr("class", "g")
        .attr("transform", function(d) { return "translate(" + x0(d.Month) + ",0)"; });

      month.selectAll(".success")
        .data(function(d) { return d.cats; })
        .enter().append("rect")
        .attr("class", "bar success")
        .attr("x", function(d) { return x1(d.name); })
        .attr("y", function(d) { return y(d.success); })
        .attr("width", x1.rangeBand())
        .attr("height", function(d) { return height - y(d.success); })
        .style("fill", colorSuccess );

      month.selectAll(".failure")
        .data(function(d) { return d.cats; })
        .enter().append("rect")
        .attr("class", "bar failure")
        .attr("height", function(d) { return height - y(d.failure); })
        .attr("width", x1.rangeBand())
        .attr("x", function(d) { return x1(d.name); })
        .attr("y", function(d) { return y(d.success) - (height - y(d.failure)); })
        .style("fill", colorFailure );

      month.selectAll("text")
        .data(function(d) { return d.cats; })
        .enter().append("text")
        .attr("y", function(d) { return x1(d.name) + x1.rangeBand() / 2 + 4; })
        .attr("x", -height + 2)
        .style("text-anchor", "begin")
        .style("font-size", 10)
        .attr("transform", "rotate(-90)")
        .text(function(d) { if (d.name == "NSI") return ""; else return d.name; })
        .style("fill", "white");
    });
  };

  return {
    init: init
  };
}();

app.register(app.graph);