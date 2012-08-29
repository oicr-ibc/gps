require [
	'cs!../app/models/sample'
	'cs!../app/models/steps'
], (Sample, Steps) ->
	'use strict'

	describe "Sample", ->
		describe "Unit Tests", ->
			sample = {}

			beforeEach ->
			    sample = new Sample {identifier: 'SAM-001'}, {url: '/tracker/api/study/GPS/sample/SAM-001'}

			it "should have an identifier", ->
				expect(sample.get('identifier')).toEqual 'SAM-001'

			it "should have a collection of steps", ->
				expect(sample.steps.url).toEqual "#{sample.url}/step"

		describe "Integration Tests", ->
			it "should have an identifier", ->
				expect('GPS').toEqual 'GPS'
