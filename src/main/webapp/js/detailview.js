(function($) {
    $.fn.detailView = function(detailsUrl, fields, headers, closeImageUrl, options) {
        var settings = $.extend({}, $.fn.detailView.defaults, options);

        return this.each(function() {
            var $self = $(this),
                $sourceRow = $self.closest("tr"),
                $hideSelf = $("<div/>").css({float: "left", width: "21px", height: "15px"}).hide(),
                nrOfColumns = $sourceRow.find("td").length,
                elementId = $self.next().attr('href').split('=').pop(),

                showDetails = function() {
                    $.getJSON(detailsUrl.replace("{}", elementId), function(data) {
                        var $detailTable = $("<table/>", {"class" : "zebra-striped"}).append($("<thead/>").append(createHeaders()));

                        $.each(data, function(i, port) {
                            $detailTable.append(createRow(port));
                        });

                        var $closeLink = $("<a/>", {
                            id: "closeLink",
                            href: "#",
                            title: $self.attr("data-original-title").replace("Show", "Hide")
                        }).append($("<img/>", {
                            src : closeImageUrl
                        })).click(
                            function(event) {
                                event.preventDefault();
                                hideDetails();
                            }
                        ).twipsy();

                        var $newRow = $("<tr/>", {
                            "class" : "detailview"
                        }).append($("<td/>", {
                            colspan : nrOfColumns - 1
                        }).append($detailTable)).append($("<td/>").append($closeLink));

                        $self.twipsy("hide");
                        $self.hide();
                        $hideSelf.show();

                        $newRow.css("opacity", "0");
                        $sourceRow.after($newRow);
                        $newRow.animate({opacity: "1"}, settings.animationDelay);
                    });
                },

                hideDetails = function() {
                    var $detailRow = $sourceRow.next(),
                        $closeLink = $detailRow.find("a#closeLink");

                    $closeLink.twipsy("hide");
                    $detailRow.fadeOut(settings.animationDelay, function() {
                        // hide twipsy again, could be possible that it appeared just after click
                        $closeLink.twipsy("hide");
                        $detailRow.remove();
                        $hideSelf.hide();
                        $self.show();
                    });
                };

            $self.after($hideSelf);

            if (settings.hide($sourceRow)) {
                $hideSelf.show();
                $self.hide();
            } else {
                $self.click(function(event) {
                    event.preventDefault();
                    showDetails();
                });
            }
        });

        function createHeaders() {
            var $row = $("<tr/>");
            $.each(headers, function(i, header) {
                $row.append($("<th/>", {text : header}));
            });
            if (settings.extraColumn !== null) {
                $row.append($("<th/>"));
            }
            return $row;
        }

        function createRow(jsonObject) {
            var $row = $("<tr/>");
            $.each(fields, function(i, field) {
                var value = jsonObject[field] || "-";
                $row.append($("<td/>", {text: value}));
            });
            if (settings.extraColumn !== null) {
                $row.append($("<td/>", {html: settings.extraColumn(jsonObject[settings.idProp])}));
            }
            return $row;
        }
    };

    $.fn.detailView.defaults = {
        animationDelay: 500,
        idProp: "id",
        extraColumn: null,
        hide: function($row) { return false; }
    };
})( jQuery );