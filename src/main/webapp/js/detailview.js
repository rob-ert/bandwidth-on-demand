$(function() {
    $.fn.detailView = function(detailsUrl, fields, headers, closeImageUrl) {
        $(this).click(function() {
            var self = $(this);
            var sourceRow = self.closest("tr");
            var nextRow = sourceRow.next();

            if (nextRow.hasClass("detailview")) {
                closeRow(nextRow.find("a"));
                return false;
            }

            var nrOfColumns = sourceRow.find("td").length;
            var elementId = self.next().attr('href').split('=').pop();

            $.getJSON(detailsUrl.replace("{}", elementId), function(data) {
                var portsTable = $("<table/>", {class : "zebra-striped"}).append($("<thead/>").append(createHeaders()));

                $.each(data, function(i, port) {
                    portsTable.append(createRow(port));
                });

                var closeLink = $("<a/>", {
                    href : "#",
                    title : "Hide Virtual Ports"
                }).append($("<img/>", {
                    src : closeImageUrl
                })).twipsy();

                closeLink.click(function() {
                    closeRow(this);
                    return false;
                });

                var newRow = $("<tr/>", {
                    class : "detailview"
                }).append($("<td/>", {
                    colspan : nrOfColumns - 1
                }).append(portsTable)).append($("<td/>").append(closeLink));

                newRow.fadeIn();
                sourceRow.after(newRow);
            });

            return false;
        });

        function createHeaders() {
            var row = $("<tr/>");
            $.each(headers, function(i, header) {
                row.append($("<th/>", {
                    text : header
                }));
            });
            return row;
        }

        function createRow(port) {
            var row = $("<tr/>");
            $.each(fields, function(i, field) {
                value = port[field];
                value = value == null ? "-" : value;
                row.append("<td>" + value + "</td>");
            });

            return row;
        }
        function closeRow(link) {
            row = $(link).closest("tr");
            row.fadeOut(function() {
                $(link).twipsy('hide');
                $(this).remove();
            });
        }
    };
})