var app = {};

app.modules = [];

app.register = function(module) {

    app.modules[app.modules.length] = module;

}

app.bootstrap = function() {

    $.each(app.modules, function(i, module) {
        if(typeof module.init === 'function') {
            module.init.call(module);
        }
    })

}

$(function() {
    var go = app.bootstrap();

});
