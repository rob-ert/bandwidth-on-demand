var app = app || {};

app.global = function() {

    var init = function() {

        initEventHandlers();

    };

    var initEventHandlers = function() {

        initTooltips();
        initPopovers();

    };

    var _placement = function(popup, element) {
        popup.setAttribute('data-type', element.getAttribute('data-type'))
        return 'top';
    }

    var initTooltips = function() {

       	$('[rel="tooltip"]').tooltip({
            delay: 500,
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
