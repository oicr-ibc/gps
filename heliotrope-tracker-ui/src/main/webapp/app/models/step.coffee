define [
  'underscore',
  'cs!mediator',
  'cs!models/model'
], (_, mediator, Model) ->
#  'use strict'

  class Step extends Model
    idAttribute: '_id'

    initialize: (attributes, options) ->
      @id = attributes.id
      super
      
      #console.debug 'Step#initialize', attributes, options, @