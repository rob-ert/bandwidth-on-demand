var app = app || {};

app.reservation = function() {

    var table, url, lastEventId;

    var init = function() {
        table = $('[data-component="reservation"]');
        url = table.attr('data-url');
        lastEventId = table.attr('data-last-event-id');

        if (table.length && url) {
            app.loadPlugin(!$.socket, app.plugins.jquery.socket, startPushConnection);
        }
    };

    var startPushConnection = function() {
        $.socket.defaults.transports = ["longpoll"];
        $.socket(url, {lastEventId: lastEventId}).message(processEvent);
    };

    var processEvent = function(event) {
        app.message.showInfo(event.message);
        updateReservationRow(event.reservationId, event.status, event.deletable, event.deleteTooltip);
    };

    var updateReservationRow = function(id, newStatus, deletable, deleteTooltip) {
        var row = $('tr[data-reservationId="'+id+'"]'),
                cell = row.find('td.status').wrapInner('<span></span>'),
                span = cell.find('span'),
                actionCell = row.find('.actions-column');

        if (!deletable) {
            actionCell.find('a .icon-remove').parent().hide();
            actionCell.find('span .icon-remove').parent().show().attr("data-original-title", deleteTooltip).tooltip('fixTitle');
        } else {
            actionCell.find('span .icon-remove').parent().hide();
            actionCell.find('a .icon-remove').parent().show().attr('data-original-title', deleteTooltip).tooltip('fixTitle');
        }

        cell.css({ overflow: 'hidden' });

        span.animate(
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
