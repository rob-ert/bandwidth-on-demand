var app = app || {};

app.form = function(){

    var init = function() {

        initEventHandlers();
        initPlugins();

    };

    var initEventHandlers = function() {

        initFormLinks();
        initDropdownFilter();
        initPhysicalPortSelector();
        initAcceptDeclineSelector();
        initPhysicalPortSelectorForVirtualPort();
        initApiSabSelector();

        // create reservation
        initBandwidthSelector();
        initStartNow();
        initProtectionType();
        initInputCopy();
    };

    var initPlugins = function() {

        initDropDownReload();
        initDatepickers();
        initAutoSuggest();
    };

    var initInputCopy = function() {
       $('input.input-copy').on('click', function(event) {
           event.target.focus();
           event.target.select();
       });
    };

    var initFormLinks = function() {

        $('a[data-form]').on('click', function(event) {

            var errorMessage = 'Sorry, action failed.',
                successMessage = $(this).attr('data-success'),
                reload = $(this).attr('data-reload');

            var post = function(url, data) {
                $.post(url, data)
                .success(function() {
                    if (successMessage) {
                        app.message.showInfo(successMessage);
                    }
                    if (reload === 'true') {
                        window.location.reload(true);
                    }
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

        });

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
            return Math.min.apply(null, selectedValues);
        };

        var calculateBandwidth = function(multiplier) {
            return Math.floor(getMaxBandwidth() * multiplier);
        };

        var activateBandwidthButton = function() {

            $('[data-component="bandwidth-selector"] button').each(function(i, element) {
                if(calculateBandwidth(parseFloat(element.getAttribute('data-bandwidth-multiplier'))) === bandwidth) {
                    $(element).addClass('active');
                } else {
                    $(element).removeClass('active');
                }
            });

        };

        $('[data-component="bandwidth-selector"] input').on('change', function(event) {

            bandwidth = parseInt(event.target.value);

            activateBandwidthButton(bandwidth / getMaxBandwidth());

            event.preventDefault();

        });

        $('[data-component="bandwidth-selector"]').on('click', '.btn', function(event) {

            event.preventDefault();

            if(event.clientX === 0) { return; } // For some weird reason, this 'click' event was also fired on change of the input...?!

            bandwidth = calculateBandwidth(parseFloat(event.target.getAttribute('data-bandwidth-multiplier')));

            input.val(Math.floor(bandwidth)).trigger('change');

        });

        $('[data-component="bandwidth-selector-source"]').on('change', function() {

            $('[data-component="bandwidth-selector"] button').eq(1).trigger('click');

        });

        activateBandwidthButton();

    };

    var initDropDownReload = function() {

        var dropdown = $('[data-component="team-selector"]');

        if(dropdown.length) {

            app.loadPlugin(!$.fn.dropdownReload, app.plugins.jquery.dropdownReload, function() {
                attachDropdownReloadPlugin(dropdown);
            });

        }

    };

    var attachDropdownReloadPlugin = function(dropdown) {

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

    };

    var initDatepickers = function() {

        var datepickers = $('.input-datepicker');

        if(datepickers.length) {

            app.loadPlugin(!$.fn.datepicker, app.plugins.jquery.datepicker, function() {
                attachDatepickerPlugin(datepickers);
            });

        }
    };

    var attachDatepickerPlugin = function(datepickers) {

        datepickers.datepicker({
            format: 'yyyy-mm-dd',
            autoclose: true
        });

    };

    var initStartNow = function() {

        $('[data-component="start-now"]').each(function(i, item) {

            var component = $(item),
                inputs = component.find(':text'),
                dateInput = inputs.eq(0),
                timeInput = inputs.eq(1),
                date = dateInput.val(),
                time = timeInput.val();

            component.on('click', ':checkbox', function() {
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

            if (date.length == 0 && time.length == 0) {
              component.find(':checkbox').click();
            }

        });

    };

    var initAutoSuggest = function() {

        var asElements = $('[data-component="autoSuggest"]');

        if(asElements.length) {

            app.loadPlugin(!$.fn.autoSuggest, app.plugins.jquery.autoSuggest, function() {
                attachAutoSuggestPlugin(asElements);
            });

        }

    };

    var attachAutoSuggestPlugin = function(asElements) {

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
                neverSubmit: true,
                preFill: [{id:preFillId, name:preFillName}],
                selectionLimit: 1,
                inputName: 'instituteId',
                selectionAdded: function(elem) {
                    $('input[name="'+inputName+'"]').hide();
                },
                resultClick: function(data) {
                    instituteSelected(data.attributes);
                },
                selectionRemoved: function(elem) {
                    elem.remove();
                    $('input[name="'+inputName+'"]').show();
                    $("input[name='instituteId']").val('');

                    instituteDeselected(elem);
                },
                end: function() {
                    setup_done = true;
                }
            });

        });

    };

    var initDropdownFilter = function() {

        // Do new get based on selected item
        $('[data-component="dropdown-filter"] select').on('change', function(event) {
            var element = $(event.target);
            var newPath = element.closest('form').attr('action') + element.val();

            window.location.href = "//" + window.location.host + newPath;
        });

    };

    var initPhysicalPortSelector = function() {
        $('[data-component="physicalport-selector"]').on('change', function() {
            var selected = $(this).find('option:selected');
            $("#_nocLabel_id").val(selected.attr("data-noclabel"));
            $("#_bodPortId_id").val(selected.attr("data-portid"));
        });
    };

    var initPhysicalPortSelectorForVirtualPort = function() {
        var selects = $('[data-component="physicalport-selector-for-virtualport"]');

        selects.each(function(i, item){
            var select = $(item),
               fieldGroup = $('#'+select.attr("data-field"));

            select.on('change', function() {
                var show = select.find(":selected").attr("data-hasvlan") === 'true';

                if (show) {
                   fieldGroup.show();
                } else {
                    //empty existing value, to prevent posting
                    fieldGroup.find("input").val('');
                    fieldGroup.hide();
                }
            });

            select.trigger('change');
        });
    };

    var initAcceptDeclineSelector = function() {
        var component = $('[data-component="accept-decline-selector"]');

        if (component.length) {
            var acceptLabel = component.attr('data-accept');
            var declineLabel = component.attr('data-decline');
            var submitButton = $('input[type="submit"]');

            component.find(':radio').on('change', function() {
                $('#accept-form').toggle();
                $('#decline-form').toggle();
                submitButton.val(submitButton.val() === declineLabel ? acceptLabel : declineLabel);
            });

            var state = component.find(':radio:checked').val();
            $('#' + (state === 'accept' ? 'decline' : 'accept') + '-form').toggle();
            submitButton.val(eval(state+'Label'));
        }
    };

    var initProtectionType = function() {
        var component = $('[data-component="protection-type"]');

        if (component.length) {
            var hidden = component.find(":hidden");

            component.on('click', ':checkbox', function(event) {
                if ($(event.target).attr("checked")) {
                    hidden.val("PROTECTED");
                } else {
                    hidden.val("UNPROTECTED");
                }
            });

            if (hidden.val() == "PROTECTED") {
                component.find(":checkbox").attr("checked", "checked");
            }
        }
    };

    var initApiSabSelector = function() {
        var component = $('[data-component="authorization-type"]');

        if (component.length) {
            var adminGroupControls = component.parents('form').find('#adminGroup');
            var adminGroupOutput = adminGroupControls.find('output');
            var adminGroupHidden = adminGroupControls.find('input[type="hidden"]');
            var adminGroupInput = adminGroupControls.find('input[type="text"]');
            var sabPrefix = component.attr("data-sabprefix");

            component.on('change', ':radio', function(event) {
                if ($(event.target).val() === "sab") {
                    sabSelected();
                } else {
                    apiSelected();
                }
            });

            if ($('[data-component="authorization-type"]').find(':checked').val() === "sab") {
                sabSelected();
            } else {
                apiSelected();
            }
        }

        function sabSelected() {
          var shortName = $('input[name="shortName"]').val();
          if (shortName.length > 0) {
              var adminGroup = sabPrefix + shortName;
              var output = adminGroup;
          } else {
              var adminGroup = "";
              var output = "Choose an institute";
          }

          adminGroupOutput.text(output).show();
          adminGroupHidden.val(adminGroup).removeAttr("disabled");
          adminGroupInput.attr("disabled", "true").hide();
        }

        function apiSelected() {
          adminGroupOutput.text("").hide();
          adminGroupInput.removeAttr("disabled").val("").show();
          adminGroupHidden.attr("disabled", "true")
        }
    };

    var instituteDeselected = function() {
        $('input[name="shortName"]').val('');
        var selectedAuthMethod = $('[data-component="authorization-type"]').find(':checked')

        if (selectedAuthMethod.val() === "sab") {
            selectedAuthMethod.trigger('change');
        }
    };

    var instituteSelected = function(institute) {
        $('input[name="shortName"]').val(institute.shortName);
        var selectedAuthMethod = $('[data-component="authorization-type"]').find(':checked')

        if (selectedAuthMethod.val() === "sab") {
            selectedAuthMethod.trigger('change');
            $('#_managerEmail_id').focus();
        } else {
            $('#_adminGroup_id').focus();
        }
    };

    return {
        init: init
    };

}();

app.register(app.form);