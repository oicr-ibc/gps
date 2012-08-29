define [
  'cs!mediator'
  'cs!models/container'
  'cs!models/collection'
  'cs!models/sample'
], (mediator, Container, Collection, Sample) ->
#  'use strict'

  class Samples extends Collection
    model: Sample
    
  class SamplesContainer extends Container

    initialize: (attributes, options) -> 
      console.debug 'SamplesContainer#initialize', attributes, options, @
      super
      @url = "#{options.url}/sample"

      @set "data", new Samples @url

