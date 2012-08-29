define [
  'chaplin/controllers/controller',
  'cs!models/navigation',
  'cs!views/navigation_view'
], (Controller, Navigation, NavigationView) ->

#  'use strict'

  class NavigationController extends Controller

    initialize: ->
      super
      #console.debug 'NavigationController#initialize'
      @navigation = new Navigation()
      @view = new NavigationView model: @navigation
