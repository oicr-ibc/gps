define [
  'cs!mediator',
  'cs!views/collection_view',
  'cs!views/study/partials/table_row_view',
  'text!templates/study/partials/table_body.hbs'
], (mediator, CollectionView, StudyTableRowView, template) ->
#  'use strict'
  
  class StudyTableBodyView extends CollectionView  
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null
    
    autoRender: true


    # The most important method a class derived from CollectionView
    # must overwrite.
    getView: (item) ->
      # Instantiate an item view
      new StudyTableRowView model: item