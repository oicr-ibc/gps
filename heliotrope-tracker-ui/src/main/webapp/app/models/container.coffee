define [
  'chaplin/models/model'
], (CModel) ->
#  'use strict'

  class Container extends CModel

    parse: (r) ->
      console.debug 'Container#parse', r
      
      @get("data").reset(r.data)
      # Need to delete this or is will overwrite the Collection with an Array
      delete r.data
      r