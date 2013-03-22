DROP TABLE IF EXISTS 
  known_gene_cosmic_genes,
  known_mutation_cosmic_mutations,
  known_tumour_type_cosmic_tumour_types;
  
DROP TABLE IF EXISTS
  cosmic_mutation_frequency;
  
DROP TABLE IF EXISTS
  cosmic_gene,
  cosmic_mutation,
  cosmic_tumour_type;

DROP TABLE IF EXISTS
  my_table,
  test_report;

DROP TABLE IF EXISTS
  acl_class,
  acl_entry,
  acl_sid,
  acl_object_identity;

DROP TABLE IF EXISTS
  assay;
  
SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'mergegps' AND ENGINE != 'InnoDB';

ALTER TABLE panel                 ENGINE = 'InnoDB';
ALTER TABLE known_mutation        ENGINE = 'InnoDB';
ALTER TABLE observed_mutation     ENGINE = 'InnoDB';
ALTER TABLE process               ENGINE = 'InnoDB';
ALTER TABLE report                ENGINE = 'InnoDB';
ALTER TABLE report_document       ENGINE = 'InnoDB';
ALTER TABLE report_mutations      ENGINE = 'InnoDB';
ALTER TABLE run_assay             ENGINE = 'InnoDB';
ALTER TABLE run_sample            ENGINE = 'InnoDB';

DELETE FROM run_assay WHERE status != 'YES';

ALTER TABLE observed_mutation
ADD COLUMN run_sample_id bigint(20) NOT NULL REFERENCES run_sample(id);

-- Now update the column

UPDATE observed_mutation JOIN run_assay ON observed_mutation.run_assay_id = run_assay.id
SET observed_mutation.run_sample_id = run_assay.run_sample_id;

ALTER TABLE observed_mutation
DROP COLUMN run_assay_id;

-- Next stage is to merge the knowledge base information. This does depend on a little sophistication,
-- and some of it is not entirely trivial given the bootstrapping used to get the data into the 
-- application itself.

-- Reduce merging pain by noting all mutations that have not been observed. We will, obviously
-- create them again later. 

ALTER TABLE observed_mutation
ADD COLUMN chromosome CHAR(2),
ADD COLUMN start INT(11),
ADD COLUMN stop INT(11),
ADD COLUMN var_allele VARCHAR(45),
ADD COLUMN mutation VARCHAR(45);

ALTER TABLE known_mutation
MODIFY COLUMN chromosome CHAR(2);

UPDATE observed_mutation om
JOIN known_mutation km ON om.known_mutation_id = km.id
SET om.chromosome = km.chromosome,
om.start = km.start,
om.stop = km.stop,
om.var_allele = km.var_allele,
om.mutation = km.mutation;

-- Now we really need the knowledge base from a new system. This should contain all the newly
-- created, installed, and loaded mutations and all their friends, reports, and so on. When
-- this is done, we can re-connect observed with known mutations using the chromosome,
-- start, stop, and variant allele information. Best way to do this is start the app in test
-- mode against MySQL with a create database. 

-- A few mutations don't exist in COSMIC. We need to be careful here. First, let's delete 
-- synonymous mutations. 

DELETE FROM observed_mutation
WHERE left(mutation, 1) = right(mutation, 1)
AND mutation REGEXP '^[[:alpha:]][[:digit:]]+[[:alpha:]]$';

ALTER TABLE observed_mutation
DROP COLUMN known_mutation_id;

DROP TABLE mutation_confirmation, 
           mutation_characteristics,
           known_mutation_frequency,
           known_mutation,
           known_tumour_type,
           known_gene,
           gene_characteristics,
           clinical_significance,
           agent_effectiveness,
           agent_sensitivity;

CREATE TABLE known_gene LIKE cosmic.known_gene;
INSERT INTO known_gene SELECT * FROM cosmic.known_gene;

CREATE TABLE known_tumour_type LIKE cosmic.known_tumour_type;
INSERT INTO known_tumour_type SELECT * FROM cosmic.known_tumour_type;

CREATE TABLE known_mutation LIKE cosmic.known_mutation;
INSERT INTO known_mutation SELECT * FROM cosmic.known_mutation;

CREATE TABLE gene_characteristics LIKE cosmic.gene_characteristics;
INSERT INTO gene_characteristics SELECT * FROM cosmic.gene_characteristics;

CREATE TABLE mutation_characteristics LIKE cosmic.mutation_characteristics;
INSERT INTO mutation_characteristics SELECT * FROM cosmic.mutation_characteristics;

CREATE TABLE agent_effectiveness LIKE cosmic.agent_effectiveness;
INSERT INTO agent_effectiveness SELECT * FROM cosmic.agent_effectiveness;

CREATE TABLE agent_sensitivity LIKE cosmic.agent_sensitivity;
INSERT INTO agent_sensitivity SELECT * FROM cosmic.agent_sensitivity;

CREATE TABLE clinical_significance LIKE cosmic.clinical_significance;
INSERT INTO clinical_significance SELECT * FROM cosmic.clinical_significance;

CREATE TABLE known_mutation_frequency LIKE cosmic.known_mutation_frequency;
INSERT INTO known_mutation_frequency SELECT * FROM cosmic.known_mutation_frequency;

CREATE TABLE mutation_confirmation LIKE cosmic.mutation_confirmation;
INSERT INTO mutation_confirmation SELECT * FROM cosmic.mutation_confirmation;

ALTER TABLE known_mutation ADD CONSTRAINT FOREIGN KEY (known_gene_id) REFERENCES known_gene(id);
ALTER TABLE gene_characteristics ADD CONSTRAINT FOREIGN KEY (gene_id) REFERENCES known_gene(id);
ALTER TABLE agent_sensitivity ADD CONSTRAINT FOREIGN KEY (mutation_id) REFERENCES known_mutation(id);
ALTER TABLE agent_effectiveness ADD CONSTRAINT FOREIGN KEY (mutation_id) REFERENCES known_mutation(id);
ALTER TABLE mutation_characteristics ADD CONSTRAINT FOREIGN KEY (mutation_id) REFERENCES known_mutation(id);
ALTER TABLE known_mutation_frequency
  ADD CONSTRAINT FOREIGN KEY (tumour_type_id) REFERENCES known_tumour_type(id),
  ADD CONSTRAINT FOREIGN KEY (mutation_id) REFERENCES known_mutation(id);
ALTER TABLE clinical_significance
  ADD CONSTRAINT FOREIGN KEY (tumour_type_id) REFERENCES known_tumour_type(id),
  ADD CONSTRAINT FOREIGN KEY (mutation_id) REFERENCES known_mutation(id);

-- And now, the end is near, and so we face, the final queries

ALTER TABLE observed_mutation
ADD COLUMN known_mutation_id BIGINT(20);

UPDATE observed_mutation om 
JOIN known_mutation km ON km.chromosome = om.chromosome AND km.start = om.start AND km.stop = om.stop AND km.var_allele = om.var_allele
SET om.known_mutation_id = km.id;

ALTER TABLE observed_mutation 
MODIFY COLUMN known_mutation_id BIGINT(20) NOT NULL,
ADD CONSTRAINT FOREIGN KEY (known_mutation_id) REFERENCES known_mutation(id);


