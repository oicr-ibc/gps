define [
  'cs!views/view'
  'text!templates/sample/partials/table_row.hbs'
], (View, template) ->
#  'use strict'

  class SampleTableRowView extends View
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    tagName: 'tr'
    className: 'sample'