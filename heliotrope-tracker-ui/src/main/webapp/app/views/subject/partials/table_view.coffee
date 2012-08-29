define [
  'cs!mediator',
  'cs!views/view',
  'cs!views/subject/partials/table_body_view',
  'text!templates/subject/partials/table.hbs'
], (mediator, View, SubjectTableBodyView, template) ->
#  'use strict'
  
  class SubjectTableView extends View  
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    autoRender: false
    

    initialize: ->
      console.debug 'SubjectTableView#initialize', @
      super # Will render the list itself and all items
      
      # Render again when the model is resolved
      @modelBind 'change', @render
      #@model.fetch()
      

    render: ->
      console.debug 'SubjectTableView#render', @
      super
      @subview 'TableBody', new SubjectTableBodyView {collection: @model.get("data"), el: @.$("tbody")}
