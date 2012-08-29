define [
  'cs!mediator'
  'cs!models/study'
  'cs!views/view'
  'text!templates/study/create.hbs'
], (mediator, Study, View, template) ->
#  'use strict'

  class StudyCreateView extends View
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    id: 'study'
    containerSelector: '#content-container'
    autoRender: true

    initialize: ->
      console.debug 'StudyCreateView#initialize', @
      super
      @delegate 'submit', 'form', @save

    save: (event) ->
      event.preventDefault()
      event.stopImmediatePropagation()

      study = new Study @.$(event.target).serializeObject()

      study.save {},
        success: (data) -> 
          Backbone.history.navigate study.get('url'), true

      return