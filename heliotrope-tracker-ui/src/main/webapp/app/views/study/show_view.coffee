define [
  'cs!mediator'
  'cs!views/view'
  'cs!views/study/show/sidebar_view'
  'cs!views/study/show/page_header_view'
  'cs!views/study/show/summary_view'
  'cs!views/subject/partials/table_view'
  'cs!views/sample/partials/table_view'
  'text!templates/study/show.hbs'
], (mediator, View, Sidebar, PageHeaderView, SummaryView, SubjectTableView, SampleTableView, template) ->
#  'use strict'

  class StudyShowView extends View
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    id: 'study-view'
    containerSelector: '#content-container'
    containerMethod: 'html'
    autoRender: true


    initialize: ->
      console.debug 'StudyShowView#initialize', @model
      super
      
      # Render again when the model is resolved
      #@modelBind 'change', @render
      @model.fetch()
      @model.get("subjects").fetch()
      @model.get("samples").fetch()

      # The filter could be generalized using better ids in the html,
      # like id="samples/subjects", but those are already used somewhere else
      # so they would have to be changed there too.   
      @delegate 'submit', 'form#subjects-filter', @subjectFilter
      @delegate 'submit', 'form#samples-filter', @sampleFilter
      @delegate 'click', 'span.clear', @clearFilter
    

    subjectFilter: (event) ->
      console.debug 'StudyShowView#subjectFilter', event
      event.preventDefault()
      event.stopImmediatePropagation()
      
      filter = @.$(event.target).serializeObject().filter
      @model.get("subjects").fetch({data: {filter}})  

    
    sampleFilter: (event) ->
      console.debug 'StudyTableView#sampleFilter', event
      event.preventDefault()
      event.stopImmediatePropagation()
      
      filter = @.$(event.target).serializeObject().filter
      @model.get("samples").fetch({data: {filter}})  
    

    clearFilter: (event) ->
      console.debug 'StudyTableView#clearFilter', event
      
      @.$(event.target).parent().siblings('input').eq(0).val('')
      @.$(event.target).parents('form').submit()  


    render: ->
      console.debug 'StudyShowView#render', @model
      super

      @subview 'Sidebar', new Sidebar {@model, el: @.$("#sidebar")}
      @subview 'PageHeader', new PageHeaderView {@model, el: @.$("#page-header-view")}
      @subview 'Summary', new SummaryView {@model, el: @.$("#summary-view")}
      @subview 'Subjects', new SubjectTableView {model: @model.get("subjects"), el: @.$("#subjects-table")}
      @subview 'Samples', new SampleTableView {model: @model.get("samples"), el: @.$("#samples-table")}
