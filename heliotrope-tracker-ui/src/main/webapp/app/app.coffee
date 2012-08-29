define [
  'cs!mediator'
  'chaplin/application'
  'cs!controllers/navigation_controller'
  'cs!routes'
], (mediator, Application, NavigationController, routes) ->
#  'use strict'
  
  # The application bootstrapper.
  # You should find a better name for your application.
  class App extends Application

    # Set your application name here so the document title is set to
    # “Controller title – Site title” (see ApplicationView#adjustTitle)
    title: 'Heliotrope'

    initialize: ->
      #console.debug 'ExampleApplication#initialize'

      # This creates the ApplicationController and ApplicationView
      super

      # Instantiate common controllers
      # ------------------------------
      
      # These controllers are active during the whole application runtime.
      new NavigationController()

      # Initialize the router
      # ---------------------

      # This creates the mediator.router property and
      # starts the Backbone history.
      @initRouter routes

      # Finish
      # ------

      # Freeze the application instance to prevent further changes
      Object.freeze? this
