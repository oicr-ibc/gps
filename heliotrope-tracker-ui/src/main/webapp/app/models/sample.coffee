define [
  'underscore',
  'cs!mediator',
  'cs!models/model'
  'cs!models/steps'
], (_, mediator, Model, Steps) ->
#  'use strict'

  class Sample extends Model
    urlRoot: '/tracker/api/'
    
    initialize: (attributes, options) ->
      #console.debug 'Sample#initialize', attributes, options, @
      super
      @identifier = attributes.identifier
      @url = @urlRoot + options.url