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

        /* dev/stub code */
        if(url.indexOf('stub') !== -1) {
            setTimeout(function() {
                $.getJSON(url, processEvent);
            }, 1000)
            return;
        }
        /* dev/stub code */

        $.socket(url, {
            transports: "longpoll"
        })
        .open(function(){})
        .message(function(data) {
            processEvent(data);
        })
        .close(function(){});

    };

    var processEvent = function(event) {

        app.message.showInfo(event.message);
        updateReservationRow(event.id, event.status);

    };

    var updateReservationRow = function(id, newStatus) {

        var row = $('tr[data-reservationId="'+id+'"]'),
            cell = row.find('td').eq(3).wrapInner('<span></span>'),
            span = cell.find('span');

        cell.css({
            overflow: 'hidden'
        })

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
