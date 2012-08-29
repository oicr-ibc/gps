define [
  'cs!mediator'
  'cs!views/view'
  'cs!views/study/partials/table_view'
  'text!templates/study/list.hbs'
], (mediator, View, StudyTableView, template) ->
#  'use strict'
  
  class StudyListView extends View  
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    id: 'studies-view'
    containerSelector: '#content-container'
    containerMethod: 'html'
    autoRender: true


    initialize: ->
      console.debug 'StudyListView#initialize', @
      super # Will render the list itself and all items
      
      @model.fetch()

      @delegate 'submit', 'form#study-filter', @filter
      @delegate 'click', 'span.clear', @clearFilter


    filter: (event) ->
      console.debug 'StudyListView#filter', event
      event.preventDefault()
      event.stopImmediatePropagation()
      
      filter = @.$(event.target).serializeObject().filter
      @model.fetch({data: {filter}})      
   

    clearFilter: (event) ->
      console.debug 'StudyListView#clearFilter', event
      event.preventDefault()
      event.stopImmediatePropagation()
      
      @.$(event.target).parent().siblings('input').eq(0).val('')
      @.$(event.target).parents('form').submit()


    render: ->
      console.debug 'StudyListView#render', @
      super
      @subview 'Studies', new StudyTableView {@model, el: @.$("#studies-table")}