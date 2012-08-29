define [
  'cs!mediator',
  'cs!views/view',
  'cs!views/sample/partials/table_body_view',
  'text!templates/sample/partials/table.hbs'
], (mediator, View, SampleTableBodyView, template) ->
#  'use strict'
  
  class SampleTableView extends View  
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    autoRender: false
    

    initialize: ->
      console.debug 'SampleTableView#initialize', @
      super # Will render the list itself and all items
      
      # Render again when the model is resolved
      @modelBind 'change', @render
      #@model.fetch()
      

    render: ->
      console.debug 'SampleTableView#render', @
      super
      @subview 'TableBody', new SampleTableBodyView {collection: @model.get("data"), el: @.$("tbody")}
