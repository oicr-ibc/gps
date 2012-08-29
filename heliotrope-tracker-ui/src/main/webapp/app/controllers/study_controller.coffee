define [
  'chaplin/controllers/controller'
  'cs!models/study'
  'cs!models/studies'
  'cs!views/study/list_view'
  'cs!views/study/show_view'
  'cs!views/study/create_view'
], (Controller, Study, Studies, StudyListView, StudyShowView, StudyCreateView) ->
#  'use strict'

  class StudyController extends Controller

    title: 'Studies'

    historyURL: (params) ->
      if params.id then "study/#{params.id}" else 'studies'

    list: (params) ->
      #console.debug 'StudyController#list', params
      @model = new Studies()
      @view = new StudyListView {@model}

    show: (params) ->
      #console.debug 'StudyController#show', params
      @title = params.id
      @model = new Study {identifier: params.id}
      @view = new StudyShowView {@model}

    create: (params) ->
      #console.debug 'StudyController#create', params
      @title = 'Create Study'
      @view = new StudyCreateView params