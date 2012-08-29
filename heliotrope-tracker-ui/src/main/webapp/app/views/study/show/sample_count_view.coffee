define [
  'cs!views/view'
  'text!templates/study/show/sample_count.hbs'
], (View, template) ->
#  'use strict'

  class SampleCountView extends View
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    autoRender: false


    initialize: ->
      console.debug 'SampleCountView#initialize', @model
      super
      
      # Render again when the model is rendersolved
      @modelBind 'change', @render


    render: ->
      console.debug 'SampleCountView#render', @model
      super unless @.$(".controls").text()