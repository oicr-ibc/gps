define [
  'chaplin/controllers/controller'
  'cs!models/subject'
  'cs!models/subjects'
  'cs!views/subject/show_view'
  'cs!views/subject/create_view'
], (Controller, Subject, Subjects, ShowView, CreateView) ->
#  'use strict'

  class SubjectController extends Controller

    title: 'Subjects'

    historyURL: (params) ->
      if params.id then "subject/#{params.id}" else 'subjects'

    show: (params) ->
      console.debug 'SubjectController#show', params
      @title = params.id
      @model = new Subject {identifier: params.id}, {url: params.path}
      @view = new ShowView {@model}

    create: (params) ->
      console.debug 'SubjectController#create', params
      @title = 'Create Subject'
      @model = new Subject {study: {identifier: params.id}}, {url: params.path}
      @view = new CreateView {@model}