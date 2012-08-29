define [
  'Handlebars',
  'cs!mediator'
  'cs!views/view'
  'text!templates/form/show.hbs'
], (Handlebars, mediator, View, template) ->
#  'use strict'

  class FormView extends View
    
    # Save the template string in a prototype property.
    # This is overwritten with the compiled template function.
    # In the end you might want to used precompiled templates.
    template: template
    template = null

    autoRender: false

    initialize: ->
      console.debug 'FormView#initialize', @model
      super
      
      # Render again when the model is resolved
      @modelBind 'change', @render
      @model.fetch()

    input: (element)-> 
      """
      <div class="control-group">
        <label class="control-label" for="#{element.name}">#{element.label.default}</label>
        <div class="controls">
          <input type="#{element.controlType.name}" name="#{element.name}" value="" id="#{element.name}">
        </div>
      </div>
      """

    hidden: (element)-> 
      """
      <div class="controls">
        <input type="#{element.controlType.name}" name="#{element.name}" value="" id="#{element.name}">
      </div>
      """

    date: (element)->
      """
      <div class="control-group">
        <label class="control-label" for="#{element.name}">#{element.label.default}</label>
        <div class="controls">
          <input type="text" name="#{element.name}" value="" id="#{element.name}">
        </div>
      </div>
      """

    element: (element) ->
      #console.debug 'FormView#element',  element
      switch element.controlType.name
        when "text" then @input(element)
        when "hidden" then @hidden(element)
        when "date" then @date(element)
        else element.controlType.name
        
    form: (elementList) ->
      #console.debug 'FormView#form', elementList
      @element element for element in elementList

    render: ->
      #console.debug 'FormView#render', @model
      @model.set "htmlEls", @form @model.get "values"
      super
      