define [
  'cs!mediator'
  'cs!models/subject'
  'cs!models/protocol'
  'cs!views/view'
  'cs!views/form/show_view'
  'text!templates/subject/create.hbs'
], (mediator, Subject, Protocol, View, FormShowView, template) ->
#  'use strict'

  class SubjectCreateView extends View
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    id: 'subject'
    containerSelector: '#content-container'
    autoRender: true


    initialize: ->
      console.debug 'SubjectCreateView#initialize', @
      super

      @protocol = new Protocol "url": "#{@model.url}/../protocol/CreateSubject"
      
      @delegate 'submit', 'form', @save


    render: ->
      console.debug 'SubjectCreateView#render', @
      super

      @subview 'Form', new FormShowView {model: @protocol, el: @.$("#form-elements")}


    save: (event) ->
      event.preventDefault()
      event.stopImmediatePropagation()

      @model.save @.$(event.target).serializeObject(),
        success: (data) => 
          Backbone.history.navigate @model.get('url'), true
      
      return