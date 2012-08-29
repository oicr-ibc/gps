require [
	'cs!../app/models/study'
	'cs!../app/models/subjects'
	'cs!../app/models/samples'
	'cs!../app/models/steps'
], (Study, Subjects, Samples, Steps) ->
	'use strict'

	describe "Study", ->
		describe "Unit Tests", ->
			study = {}

			beforeEach ->
			    study = new Study
			    			identifier: 'GPS'
			    			description: 'Genome Potato Sandwich'

			it "should have an identifier", ->
				expect(study.get('identifier')).toEqual 'GPS'

			it "should have a description", ->
				expect(study.get('description')).toEqual 'Genome Potato Sandwich'

			it "should have a collection of subjects", ->
				expect(study.subjects.url).toEqual "#{study.url()}/subject"

			it "should have a collection of samples", ->
				expect(study.samples.url).toEqual "#{study.url()}/sample"

			it "should have a collection of steps", ->
				expect(study.steps.url).toEqual "#{study.url()}/step"

		describe "Integration Tests", ->
			it "should have an identifier", ->
				expect('GPS').toEqual 'GPS'
