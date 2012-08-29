define [
  'cs!mediator'
  'cs!models/container'
  'cs!models/collection'
  'cs!models/study'
], (mediator, Container, Collection, Study) ->
#  'use strict'

  class Studies extends Collection
    model: Study


  class StudiesContainer extends Container
    url: '/tracker/api/study/'

    defaults:
      data: new Studies()