var app = app || {};

app.message = function() {

    var showInfo = function(message) {

        var closeLink = $("<a/>", {"class": "close", href: "#", html: "&times;"});
        var record = $("<div/>", {html: message, "class": "alert-message fade in"}).append(closeLink).alert();

        $("#messages").prepend(record);

    };

    return {
        showInfo: showInfo
    };

}();

app.register(app.message);

