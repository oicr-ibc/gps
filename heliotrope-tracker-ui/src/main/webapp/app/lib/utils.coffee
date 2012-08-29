define [
  'underscore',
  'cs!mediator',
  'chaplin/lib/utils'
], (_, mediator, chaplinUtils) ->

  # Application-specific utilities
  # ------------------------------

  # Delegate to Chaplin’s utils module
  utils = chaplinUtils.beget chaplinUtils

  # Add additional application-specific properties and methods

  _(utils).extend

  utils
