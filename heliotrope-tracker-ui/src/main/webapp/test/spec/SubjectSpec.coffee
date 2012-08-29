require [
	'cs!../app/models/subject'
	'cs!../app/models/samples'
	'cs!../app/models/steps'
], (Subject, Samples, Steps) ->
	'use strict'

	describe "Subject", ->
		describe "Unit Tests", ->
			subject = {}

			beforeEach ->
			    subject = new Subject {identifier: 'SUB-001'}, {url: '/tracker/api/study/GPS/subject/SUB-001'}

			it "should have an identifier", ->
				expect(subject.get('identifier')).toEqual 'SUB-001'

			it "should have a collection of samples", ->
				expect(subject.get('samples').url).toEqual "#{subject.url}/sample"

			it "should have a collection of steps", ->
				expect(subject.get('steps').url).toEqual "#{subject.url}/step"

		describe "Integration Tests", ->
			it "should have an identifier", ->
				expect('GPS').toEqual 'GPS'
