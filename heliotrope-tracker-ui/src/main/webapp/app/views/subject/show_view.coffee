define [
  'cs!mediator'
  'cs!views/view'
  'cs!views/subject/show/sidebar_view'
  'cs!views/subject/show/page_header_view'
  'cs!views/sample/partials/table_view'
  'text!templates/subject/show.hbs'
], (mediator, View, SidebarView, PageHeaderView, SamplesTableView, template) ->
#  'use strict'

  class SubjectShowView extends View
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    id: 'subject-view'
    containerSelector: '#content-container'
    containerMethod: 'html'
    autoRender: true


    initialize: ->
      #console.debug 'SubjectShowView#initialize', @model
      super
      
      # Render again when the model is resolved
      #@modelBind 'change', @render
      @model.fetch()
      @model.get("samples").fetch()

      @delegate 'submit', 'form#samples-filter', @sampleFilter
      @delegate 'click', 'span.clear', @clearFilter


    sampleFilter: (event) ->
      console.debug 'SubjectTableView#sampleFilter', event
      event.preventDefault()
      event.stopImmediatePropagation()
      
      filter = @.$(event.target).serializeObject().filter
      @model.get("samples").fetch({data: {filter}})  
   

    clearFilter: (event) ->
      console.debug 'SubjectTableView#clearFilter', event
      event.preventDefault()
      event.stopImmediatePropagation()
      
      @.$(event.target).parent().siblings('input').eq(0).val('')
      @.$(event.target).parents('form').submit()


    render: ->
      #console.debug 'SubjectShowView#render', @model
      super

      @subview 'PageHeader', new PageHeaderView {model: @model, el: @.$("#page-header-view")}
      @subview 'Sidebar', new SidebarView {model: @model, el: @.$("#sidebar")}
      @subview 'Samples', new SamplesTableView {model: @model.get("samples"), el: @.$("#samples-table")}