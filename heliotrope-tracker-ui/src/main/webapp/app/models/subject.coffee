###
define [
  'underscore'
  'cs!mediator'
  'cs!models/model'
  'cs!models/samples'
  'cs!models/steps'
], (_, mediator, Model, Samples, Steps) ->
#  'use strict'
###
define (require, exports, module) ->
  _        = require 'underscore'
  mediator = require 'cs!mediator'
  Model    = require 'cs!models/model'
  Samples  = require 'cs!models/samples'
  Steps    = require 'cs!models/steps'

  class Subject extends Model
    urlRoot: '/tracker/api/'
    
    initialize: (attributes, options) ->
      #console.debug 'Subject#initialize', attributes, options, @
      super
      @identifier = attributes.identifier
      @url = @urlRoot + options.url

    fetch: ->
      @id = @identifier
      super
      @set "samples", new Samples {}, {@url}

      