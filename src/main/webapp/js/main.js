var app = {};

app.modules = [];

app.register = function(module) {

    app.modules[app.modules.length] = module;

}

app.bootstrap = function() {

    app.modules.forEach(function(module) {
        if(typeof module.init === 'function') {
            module.init.call(module);
        }
    })

}

$(function() {
    var go = app.bootstrap();

});
