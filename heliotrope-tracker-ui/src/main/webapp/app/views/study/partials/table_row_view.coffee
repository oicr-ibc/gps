define [
  'cs!views/view'
  'text!templates/study/partials/table_row.hbs'
], (View, template) ->
#  'use strict'

  class StudyTableRowView extends View
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    tagName: 'tr'
    className: 'study'
    autoRender: false
    
    initialize: ->
      console.debug 'StudyTableRowView#initialize', @model
      super
      
      # Render again when the model is rendersolved
      @modelBind 'change', @render
      @model.fetch()

      @model.get("subjects").on "change", @render, @
      @model.get("samples").on "change", @render, @
      
      @model.get("subjects").fetch()
      @model.get("samples").fetch()
