var app = app || {};

app.global = function() {

    var init = function() {

        initEventHandlers();
        initPlugins();

    };

    var initEventHandlers = function() {

        initUserSelection();

    };

    var initPlugins = function() {

        initTooltips();
        initPopovers();

    };

    var initUserSelection = function() {

        var form = $('.dropdown-menu');

        form.on('click', 'li', function(event) {

            var item = $(event.target).closest('li')[0];
            var roleId = item.getAttribute('data-roleId');

            $('<input>').attr({
                type: 'hidden',
                name: 'roleId',
                value: roleId
            }).appendTo(form);

            form[0].submit();

        })

    };

    var _placement = function(popup, element) {
        popup.setAttribute('data-type', element.getAttribute('data-type'))
        return 'top';
    }

    var initTooltips = function() {

       	$('[rel="tooltip"]').tooltip({
            placement: _placement
        });

    };

    var initPopovers = function() {

        $('[rel="popover"]').popover({
            placement: _placement
        });

    };

    return {
        init: init
    };

}();

app.register(app.global);
