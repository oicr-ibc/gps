define [
  'cs!views/view'
  'text!templates/study/show/subject_count.hbs'
], (View, template) ->
#  'use strict'

  class SubjectCountView extends View
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    autoRender: false

    
    initialize: ->
      console.debug 'SubjectCountView#initialize', @model
      super 

      # Render again when the model is rendersolved
      @modelBind 'change', @render

    render: ->
      console.debug 'SubjectCountView#render', @model
      super unless @.$(".controls").text()
      