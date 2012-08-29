define [
  'chaplin/controllers/controller'
  'cs!models/sample'
  'cs!models/samples'
  'cs!views/sample/show_view'
  'cs!views/sample/create_view'
], (Controller, Sample, Samples, ShowView, CreateView) ->
#  'use strict'

  class SampleController extends Controller

    title: 'Samples'

    historyURL: (params) ->
      if params.id then "sample/#{params.id}" else 'samples'

    show: (params) ->
      console.debug 'SampleController#show', params
      @title = params.id
      @model = new Sample {identifier: params.id}, {url: params.path}
      @view = new ShowView {@model}

    create: (params) ->
      #console.debug 'SampleController#create', params
      @title = 'Create Sample'
      @model = new Sample {study: {identifier: params.id}}, {url: params.path}
      @view = new CreateView {@model}