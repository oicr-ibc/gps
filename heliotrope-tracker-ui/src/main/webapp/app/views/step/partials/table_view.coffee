define [
  'cs!mediator'
  'cs!views/collection_view'
  'cs!views/step/partials/table_row_view'
  'text!templates/step/partials/table.hbs'
], (mediator, CollectionView, StepTableRowView, template) ->
#  'use strict'
  
  class StepTableView extends CollectionView  
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null
    
    tagName: 'table' # This is not directly a list but contains a list
    id: 'steps'
    className: 'table table-striped table-condensed'

    containerSelector: '#steps-table'
    
    # Append the item views to this element
    listSelector: 'tbody'
    # Fallback content selector
    fallbackSelector: '.fallback'
    # Loading indicator selector
    loadingSelector: '.loading'

    initialize: ->
      console.debug 'StepTableView#initialize', @
      super # Will render the list itself and all items
      @collection.fetch()

    # The most important method a class derived from CollectionView
    # must overwrite.
    getView: (item) ->
      # Instantiate an item view
      new StepTableRowView model: item