// Courtesy of: http://stackoverflow.com/questions/2101259/jquery-js-get-text-from-field-on-keyup-but-with-delay-for-further-typing
// This can be used as the basis for AJAX dynamic queries. 
//
// Documentation:
//	
//	Use it like so:
//	
//	    $("input.filterField").onDelayedKeyup({
//	        handler: function() {
//	            if ($.trim($("input.filterField").val()).length > 0) {
//	                //reload my data store using the filter string.
//	            }
//	        }
//	    });
//	Does a half-second delay by default.

(function($){

	$.widget("ui.onDelayedKeyup", {
	
	    _init : function() {
	        var self = this;
	        $(this.element).keyup(function() {
	            if(typeof(window['inputTimeout']) != "undefined"){
	                window.clearTimeout(inputTimeout);
	            }  
	            var handler = self.options.handler;
	            window['inputTimeout'] = window.setTimeout(function() { handler.call(self.element) }, self.options.delay);
	        });
	    },
	    options: {
	        handler: $.noop(),
	        delay: 500
	    }
	
	});
})(jQuery);
