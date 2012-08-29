define [
  'cs!views/view'
  'text!templates/study/show/page_header.hbs'
], (View, template) ->
#  'use strict'

  class PageHeaderView extends View
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    autoRender: false

    initialize: ->
      console.debug 'StudyShowView#initialize', @model
      super
      
      # Render again when the model is resolved
      @modelBind 'change', @render