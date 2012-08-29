define [
  'cs!mediator',
  'cs!views/collection_view',
  'cs!views/subject/partials/table_row_view',
  'text!templates/subject/partials/table_body.hbs'
], (mediator, CollectionView, SubjectTableRowView, template) ->
#  'use strict'
  
  class SubjectTableBodyView extends CollectionView  
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null
    
    # The most important method a class derived from CollectionView
    # must overwrite.
    getView: (item) ->
      # Instantiate an item view
      new SubjectTableRowView model: item