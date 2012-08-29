define [
  'cs!mediator'
  'cs!models/model'
], (mediator, Model) ->
#  'use strict'

  class Protocol extends Model 
    #urlRoot: "/tracker/api/"     

    initialize: ->
      console.debug 'Protocol#initialize', @
      super
      @url = @get "url"
