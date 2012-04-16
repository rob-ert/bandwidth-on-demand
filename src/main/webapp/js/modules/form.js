var app = app || {};

app.form = function(){

    var init = function() {

        initEventHandlers();
        initPlugins();

    };

    var initEventHandlers = function() {

        initFormLinks();
        initBandwidthSelector();
        initStartNow();
        initReservationFilter();

    };

    var initPlugins = function() {

        initDropDownReload();
        initDatepickers();
        initAutoSuggest();

    };

    var initFormLinks = function() {

        $('a[data-form]').on('click', function(event) {

            var errorMessage = 'Sorry, action failed.';

            var post = function(url, data) {
                $.post(url, data)
                .success(function() {
                    window.location.reload(true);
                })
                .error(function() {
                    alert(errorMessage);
                });
            };

            var element = $(event.target).closest('a')[0];
            var href = element.href;
            var data = href.replace(/[^\?]*\?/, ''); // Everything after '?'
            var url = href.replace(/\?.*/, ''); // Everything before '?'

            var isToBeConfirmed = element.getAttribute('data-confirm');

            if(isToBeConfirmed) {
                var isConfirmed = confirm(isToBeConfirmed);
                if(isConfirmed) {
                    post(url, data);
                }
            } else {
                post(url, data);
            }

            event.preventDefault();

        })

    };

    var initBandwidthSelector = function() {

        if(!$('[data-component="bandwidth-selector"]').length) {
            return;
        }

        var selectedValues,
            input = $('[data-component="bandwidth-selector"] input'),
            bandwidth = parseInt(input.val());

        var getMaxBandwidth = function() {
            selectedValues = [];
            $('[data-component="bandwidth-selector-source"]').each(function(i, element) {
                selectedValues.push(parseInt($(element).find('option:selected').attr('data-bandwidth-max')));
            });
            return Math.max.apply(null, selectedValues);
        }

        var activateBandwidthButton = function(multiplier) {

            $('[data-component="bandwidth-selector"] button').each(function(i, element) {
                if(parseFloat(element.getAttribute('data-bandwidth-multiplier')) === multiplier) {
                    $(element).addClass('active');
                } else {
                    $(element).removeClass('active');
                }
            });

        }

        $('[data-component="bandwidth-selector"] input').on('change', function(event) {

            bandwidth = parseFloat(event.target.value);

            activateBandwidthButton(bandwidth / getMaxBandwidth());

            event.preventDefault();

        });

        $('[data-component="bandwidth-selector"]').on('click', '.btn', function(event) {

            event.preventDefault();

            if(event.clientX === 0) { return; } // For some weird reason, this 'click' event was also fired on change of the input...?!

            bandwidth = getMaxBandwidth() * parseFloat(event.target.getAttribute('data-bandwidth-multiplier'));

            input.val(bandwidth).trigger('change');

        });

        $('[data-component="bandwidth-selector-source"]').on('change', function() {

            $('[data-component="bandwidth-selector"] button').eq(1).trigger('click');

        });

        activateBandwidthButton(bandwidth / getMaxBandwidth());

    }

    var initDropDownReload = function() {

        var dropdown = $('[data-component="team-selector"]');

        if(!dropdown.length) {
            return;
        }

        var url = dropdown.attr('data-url');

        dropdown.dropdownReload(
            url,
            $('[data-component="bandwidth-selector-source"]'),
            {
                afterReload: function(data) {
                    groupInJson = data;
                    $('[data-component="bandwidth-selector"] button').eq(1).trigger('click');
                },
                displayProp: "userLabel"
            }
        );

    }

    var initDatepickers = function() {

        var datepickers = $(".input-datepicker");
        if(datepickers.length) {
            $(".input-datepicker").datepicker({
                format: 'yyyy-mm-dd',
                autoclose: true
            });
        }
    }

    var initStartNow = function() {
        var date,
            time;

        $('[data-component="start-now"] :checkbox').on('click', function() {
            var inputs = $('[data-component="start-now"] :text'),
                dateInput = inputs.eq(0),
                timeInput = inputs.eq(1);

            if (dateInput.prop('disabled')) {
                dateInput.val(date).prop('disabled', false);
                timeInput.val(time).prop('disabled', false);
            } else {
                date = dateInput.val();
                time = timeInput.val();
                dateInput.val('').prop('disabled', true);
                timeInput.val('').prop('disabled', true);
            }
        });
    }

    var initAutoSuggest = function() {

        var asElements = $('[data-component="autoSuggest"]');

        asElements.each(function(i, asElement) {

            var setup_done = false;

            var element = $(asElement),
                url = element.attr('data-suggestUrl'),
                preFillId = element.attr('data-preFillId'),
                preFillName = element.attr('data-preFillName'),
                inputName = element.attr('name');

            element.autoSuggest(url, {
                selectedValuesProp: 'id',
                searchObjProps: 'name',
                selectedItemProp: 'name',
                startText: '',
                preFill: [{id:preFillId, name:preFillName}],
                selectionLimit: 1,
                inputName: 'instituteId',
                selectionAdded: function() {
                    $('input[name="'+inputName+'"]').hide();
                    if (setup_done) $("#_adminGroup_id").focus();
                },
                selectionRemoved: function(elem) {
                    elem.remove();
                    $('input[name="'+inputName+'"]').show();
                    $("input[name='instituteId']").val('');
                },
                end: function() {
                    setup_done = true;
                }
            });

        })

    }

    var initReservationFilter = function() {

        // Do new get based on selected item
        $('[data-component="reservation-filter"] select').change(function(event) {
            var element = $(event.target);
            window.location.href = element.closest('form').attr('action') + element.val();
        });

    }

    return {
        init: init
    }

}();

app.register(app.form);
