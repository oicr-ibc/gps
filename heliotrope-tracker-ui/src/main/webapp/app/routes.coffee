define ->
#  'use strict'

  # The routes for the application. This module returns a function.
  # `match` is match method of the Router
  (match) ->
  	
  	# Studies
    match 'studies', 'study#list'
    match 'study/:id', 'study#show'
    match 'study', 'study#create'

    # Subjects
    match 'study/:id/subject', 'subject#create'
    match 'study/:id/subject/:id', 'subject#show'

    # Samples
    match 'study/:id/sample', 'sample#create'
    match 'study/:id/sample/:id', 'sample#show'