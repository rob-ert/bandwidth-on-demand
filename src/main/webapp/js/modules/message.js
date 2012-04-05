var app = app || {};

app.message = function() {

    var init = function() {

        initEventHandlers();

    };

    var initEventHandlers = function() {

        initTooltips();

    };

    var initTooltips = function() {

        $.fn.tooltip.defaults.delay = 500;
       	$('[rel="tooltip"]').tooltip();

    };

    var showInfo = function(message) {

        var closeLink = $("<a/>", {class: "close", href: "#", html: "&times;"});
        var record = $("<div/>", {html: message, class: "alert-message fade in"}).append(closeLink).alert();

        $("#messages").prepend(record);

    }

    return {
        init: init,
        showInfo: showInfo
    }

}();

app.register(app.message);

