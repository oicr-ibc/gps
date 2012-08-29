define [
  'cs!mediator',
  'cs!views/view',
  'cs!views/study/partials/table_body_view',
  'text!templates/study/partials/table.hbs'
], (mediator, View, StudyTableBodyView, template) ->
#  'use strict'
  
  class StudyTableView extends View  
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    autoRender: true
    

    initialize: ->
      console.debug 'StudyTableView#initialize', @
      super # Will render the list itself and all items
      
    render: ->
      console.debug 'StudyTableView#render', @
      super
      @subview 'TableBody', new StudyTableBodyView {collection: @model.get("data"), el: @.$("tbody")}
