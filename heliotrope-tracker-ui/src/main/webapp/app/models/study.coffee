define [
  'cs!mediator'
  'cs!models/model'
  'cs!models/subjects'
  'cs!models/samples'
  'cs!models/steps'
], (mediator, Model, Subjects, Samples, Steps) ->
#  'use strict'

  class Study extends Model
    urlRoot: '/tracker/api/study/'
    url: ->
      if @.isNew() then @urlRoot else @urlRoot+@id
      
    initialize: ->
      console.debug 'Study#initialize', @
      super 

    fetch: ->
      console.debug 'Study#fetch', @
      @id = @get "identifier"
      super

      @set "subjects", new Subjects {},  {url: @url()}
      @set "samples", new Samples {}, {url: @url()}
      