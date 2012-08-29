define [
  'chaplin/models/model'
], (CModel) ->
#  'use strict'

  class Model extends CModel

    parse: (r) ->
      #console.debug 'Model#parse', r
      super.data