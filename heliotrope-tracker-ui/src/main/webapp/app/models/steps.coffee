define [
  'cs!mediator',
  'cs!models/collection',
  'cs!models/step'
], (mediator, Collection, Step) ->
#  'use strict'

  class Steps extends Collection
    model: Step
    
    initialize: (url) ->
      super
      @url = "#{url}/step"
      
      #console.debug 'Steps#initialize', @
