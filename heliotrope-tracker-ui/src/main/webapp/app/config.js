require.config({
  paths: {
    templates: '../../templates',

    jquery: '../assets/javascript/vendor/jquery/jquery.min',
    jqueryserializeobject: '../assets/javascript/vendor/jquery/jquery.ba-serializeobject',
    
    underscore: '../assets/javascript/vendor/underscore',
    
    backbone: '../assets/javascript/vendor/backbone/backbone',

    bootstrap: '../assets/javascript/vendor/bootstrap.min',
    
    Handlebars: '../assets/javascript/vendor/handlebars',
    
    json2: '../assets/javascript/vendor/json2',
    almond: '../assets/javascript/vendor/almond',
    
    CoffeeScript: '../assets/javascript/vendor/CoffeeScript',
    
    text: "../assets/javascript/vendor/require/text.min",
    use: "../assets/javascript/vendor/require/use.min",
    cs: "../assets/javascript/vendor/require/cs"
  },
  use: {
    bootstrap: {
      deps: ["jquery"]
    },
    jqueryserializeobject: {
      deps: ["jquery"]
    },
    backbone_relational: {
      deps: ["backbone"]
    }
  },
  waitSeconds: 1,
  urlArgs: 'bust=' + (new Date()).getTime()
});

require([
  'cs!app', 
  'use!bootstrap',
  'use!jqueryserializeobject',
  ], function(App) {
  var app = new App();
  return app.initialize();
});
