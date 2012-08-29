define [
  'cs!views/view'
  'cs!views/study/show/subject_count_view'
  'cs!views/study/show/sample_count_view'
  'text!templates/study/show/summary.hbs'
], (View, SubjectCountView, SampleCountView, template) ->
#  'use strict'

  class SummaryView extends View
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    autoRender: true


    render: ->
      console.debug 'SummaryView#render', @model
      super
      
      @subview 'SubjectCount', new SubjectCountView {model: @model.get("subjects"), el: @.$("#subject-count-view")}
      @subview 'SampleCount', new SampleCountView {model: @model.get("samples"), el: @.$("#sample-count-view")}