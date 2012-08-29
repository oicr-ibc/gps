define [
  'cs!mediator'
  'cs!models/container'
  'cs!models/collection'
  'cs!models/subject'
], (mediator, Container, Collection, Subject) ->
#  'use strict'

  class Subjects extends Collection
    model: Subject
    
  class SubjectsContainer extends Container

    initialize: (attributes, options) ->  
      console.debug 'SubjectsContainer#initialize', attributes, options, @
      super
      @url = "#{options.url}/subject"

      @set "data", new Subjects @url

