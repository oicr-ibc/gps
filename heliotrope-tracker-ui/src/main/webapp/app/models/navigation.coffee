define ['cs!models/model'], (Model) ->
#  'use strict'

  class Navigation extends Model
    defaults:
      items: [
        {href: '/studies', title: 'Studies'}
      ]
