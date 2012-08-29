define [
  'cs!mediator',
  'cs!views/collection_view',
  'cs!views/sample/partials/table_row_view',
  'text!templates/sample/partials/table_body.hbs'
], (mediator, CollectionView, SampleTableRowView, template) ->
#  'use strict'
  
  class SampleTableBodyView extends CollectionView  
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null
    
    # The most important method a class derived from CollectionView
    # must overwrite.
    getView: (item) ->
      # Instantiate an item view
      new SampleTableRowView model: item