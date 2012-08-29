define [
  'cs!mediator'
  'cs!views/view'
  'cs!views/sample/show/page_header_view'
  'text!templates/sample/show.hbs'
], (mediator, View, PageHeaderView, template) ->
#  'use strict'

  class SampleShowView extends View
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    id: 'sample-view'
    containerSelector: '#content-container'
    containerMethod: 'html'
    autoRender: true

    initialize: ->
      #console.debug 'SampleShowView#initialize', @model
      super
      
      # Render again when the model is resolved
      #@modelBind 'change', @render
      @model.fetch()

    render: ->
      #console.debug 'SampleShowView#render', @model
      super
      
      @subview 'PageHeader', new PageHeaderView {model: @model, el: @.$("#page-header-view")}