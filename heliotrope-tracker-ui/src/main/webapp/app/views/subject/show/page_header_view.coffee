define (require, exports, module) ->
	View     = require 'cs!views/view'
	template = require 'text!templates/subject/show/page_header.hbs'

	class PageHeaderView extends View
	    
	    # Save the template string in a prototype property.
	    # This is overwritten with the compiled template function.
	    # In the end you might want to used precompiled templates.
	    template: template
	    template = null

	    autoRender: false

	    initialize: ->
	      console.debug 'SubjectPageHeaderView#initialize', @model
	      super
	      
	      # Render again when the model is resolved
	      @modelBind 'change', @render