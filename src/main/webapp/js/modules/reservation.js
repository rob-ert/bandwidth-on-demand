var app = app || {};

app.reservation = function() {

    var init = function() {

        var table = $('[data-component="reservation"]');

        if(table.length) {
            var url = table.attr('data-url');
            setTimeout(function() {
                startPushConnection(url);
            }, 100);
        }

    }

    function startPushConnection(url) {

        $.socket(url, {
            transports: "longpoll"
       })
       .open(function() { })
       .message(function(data) {
           processEvent(data);
       })
       .close(function() { });
    };

    function processEvent(event) {
        app.message.showInfo(event["message"]);
        updateReservationRow(event["id"], event["status"]);
    }

    function updateReservationRow(id, newStatus) {
        var statusIndex = 3;
        var row = $('a[href$="id=' + id + '"]').closest("tr");
        row.addClass("highlight");
        $(row.children()[statusIndex]).animate(
            {opacity: 0},
            1000,
            function() {
                $(this).text(newStatus);
                $(this).animate({opacity: 1}, 1000,
                  function () {
                    row.removeClass("highlight");
                  }
                );
            }
        );
    }

    return {
        init: init
    };

}();

app.register(app.reservation);
