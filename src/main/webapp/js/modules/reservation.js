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
        updateReservationRow(event.id, event.status, event.deletable, event.deleteTooltip);
    };

    var updateReservationRow = function(id, newStatus, deletable, deleteTooltip) {
        var row = $('tr[data-reservationId="'+id+'"]'),
            cell = row.find('td.status').wrapInner('<span></span>'),
            span = cell.find('span'),
            actionCell = row.find('.actions-column');

        if (!deletable) {
            actionCell.find('.icon-remove').parent().remove();
            var disabledDelete = $('<span class="disabled-icon" data-type="info" rel="tooltip" title="'+deleteTooltip+'"><i class="icon-remove" /></span>');

            actionCell.append(disabledDelete);
            disabledDelete.tooltip({ placement: function(popup, element) {
                popup.setAttribute('data-type', element.getAttribute('data-type'));
                return 'top';
            } });
        }

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
