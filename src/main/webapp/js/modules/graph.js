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
        height = 450 - margin.top - margin.bottom;

    var colorSuccess = d3.scale.ordinal()
     .range(["#3479A1", "#344954", "#1B3754"]);

    var labels = d3.scale.ordinal().range(["Create Success/Failure", "Cancel Success/Failure", "NSI/GUI"]);

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
      .attr("transform", function(d, i) { return "translate(" + 155 + ", 425)"; });

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
        .style("fill", function(d) { return colorSuccess(d.name); })
        .on("mouseover", mover)
        .on("mouseout", mout)
        .attr("rel", "tooltip")
        .attr("data-original-title", function(d) { return d.success; });

      month.selectAll(".failure")
        .data(function(d) { return d.cats; })
        .enter().append("rect")
        .attr("class", "bar failure")
        .attr("height", function(d) { return height - y(d.failure); })
        .attr("width", x1.rangeBand())
        .attr("x", function(d) { return x1(d.name); })
        .attr("y", function(d) { return y(d.success) - (height - y(d.failure)); })
        .style("fill", function(d) { return d3.rgb(colorSuccess(d.name)).darker(1); })
        .on("mouseover", mover)
        .on("mouseout", mout)
        .attr("rel", "tooltip")
        .attr("data-original-title", function(d) { return d.failure; });

      sucLegend.selectAll(".legend")
        .data(catNames)
        .enter().append("rect")
        .attr("x", function(d, i) { return width - (catNames.length - i) * 185 - 18; })
        .attr("width", 18)
        .attr("height", 18)
        .style("fill", function(d) { return colorSuccess(d); });

      sucLegend.selectAll(".legend")
        .data(catNames)
        .enter().append("rect")
        .attr("x", function(d, i) { return width - (catNames.length - i) * 185; })
        .attr("width", 18)
        .attr("height", 18)
        .style("fill", function(d) { return d3.rgb(colorSuccess(d)).darker(1); });

      sucLegend.selectAll(".legend")
        .data(catNames)
        .enter().append("text")
        .attr("x", function(d, i) { return width - (catNames.length - i) * 185 + 20; })
        .attr("y", 9)
        .attr("dy", ".35em")
        .style("text-anchor", "begin")
        .text(function(d) { return labels(d); });

      $("rect[rel=tooltip]").tooltip({ placement: "left" });

    });

    function mover(d) {
      d3.select(this).transition()
        .attr("x", function(d) { return x1(d.name) - 1; })
        .attr("width", x1.rangeBand() + 2)
        .style("fill-opacity", .9);
      $(this).tooltip('show');
    }

    function mout(d) {
      d3.select(this).transition()
        .attr("x", function(d) { return x1(d.name); })
        .attr("width", x1.rangeBand())
        .style("fill-opacity", 1);
      $(this).tooltip('hide');
    }

  };

  return {
    init: init
  };
}();

app.register(app.graph);