define [
  'chaplin/models/collection'
], (CCollection) ->
#  'use strict'

  class Collection extends CCollection

    parse: (r) ->
      #console.debug 'Collection#parse', r
      {'data':model} for model in r.data
      