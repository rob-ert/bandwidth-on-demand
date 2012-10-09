var app = app || {};

app.reservation = function() {

    var table, url;

    var init = function() {

        table = $('[data-component="reservation"]');
        url = table.attr('data-url');

        if(table.length && url) {
            app.loadPlugin(!$.socket, app.plugins.jquery.socket, startPushConnection);
        }

    };

    var startPushConnection = function() {
        $.socket.defaults.transports = ["longpoll"];

        $.socket(url)
        .message(function(data) {
            processEvent(data);
        });
        //.connecting(function() {})
        //.open(function() {})
        //.close(function(reason){});
    };

    var processEvent = function(event) {
        app.message.showInfo(event.message);
        updateReservationRow(event.id, event.status);
    };

    var updateReservationRow = function(id, newStatus) {

        var row = $('tr[data-reservationId="'+id+'"]'),
            cell = row.find('td.status').wrapInner('<span></span>'),
            span = cell.find('span');

        cell.css({
            overflow: 'hidden'
        });

        span.delay(500).animate(
            {
                opacity: 0,
                marginLeft: -130
            },
            1000,
            function() {
                span.text(newStatus);
                span.animate(
                    {
                        opacity: 1,
                        marginLeft: 0
                    },
                    1000
                );
            }
        );
    };

    return {
        init: init
    };

}();

app.register(app.reservation);
