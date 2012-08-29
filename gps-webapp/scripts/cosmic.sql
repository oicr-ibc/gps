# to calculate mutation frequencies
# in this database, we are replacing the value of the field  primary_hist by hist_subtype if primary_hist == "other"

# Download file ftp://ftp.sanger.ac.uk/pub/CGP/cosmic/data_export/CosmicCompleteExport_v57_180112.tsv
# rename it to mutations.txt

-- create database cosmic_data;
use cosmic_data;

-- Ensure the database is case insensitive, although when importing initially, we will need
-- to do some case sensitive hacking about temporarily. 

-- The following db defaults are assumed
alter database
	default character set 'latin1'
	default collate 'latin1_general_ci';

drop table mutations;
create table mutations (
  id int primary key auto_increment,
  sample_name char(40) not null,
  id_sample int,
  id_tumour int,
  primary_site char(100) not null,
  site_subtype varchar(250),
  primary_hist char(150),
  hist_subtype varchar(255),
  genome_wide_screen int,
  gene_name char(40),
  accession char(30),
  hgnc char(30),
  mutation_id int,
  mutation_cds char(180),
  mutation_aa char(80),
  mutation_description varchar(255),
  mutation_zygosity char(30),
  ncbi36_position char(30),
  ncbi36_strand char(1),
  grch37_position char(30),
  grch37_strand char(1),
  somatic_status char(50),
  pubmed char(20),
  sample_source char(20),
  tumour_origin varchar(50),
  comments varchar(1024)
) ENGINE MyISAM;

-- from shell
-- mysqlimport -u root --local -p cosmic_other mutations.txt

load data local infile '~/Downloads/CosmicCompleteExport_v59_230512.tsv'
into table mutations
ignore 1 lines
(gene_name, accession, hgnc, sample_name, id_sample, id_tumour, primary_site, site_subtype, primary_hist, hist_subtype, 
 @genome_wide_screen, 
 @mutation_id, 
 mutation_cds, mutation_aa, 
 mutation_description, mutation_zygosity, 
 ncbi36_position, ncbi36_strand, grch37_position, grch37_strand, 
 somatic_status, pubmed, 
 sample_source, tumour_origin, comments)
SET mutation_id = IF(@mutation_id='',0,@mutation_id), 
    genome_wide_screen = IF(@genome_wide_screen='',0,IF(@genome_wide_screen='y',1,0));

create index mutation_sample_name on mutations(sample_name(8));
create index mutation_id_sample on mutations(id_sample);
create index mutation_sites on mutations(primary_site(8), primary_hist(8));
create index mutation_gene_mutation on mutations(gene_name(8), mutation_aa(8));
create index mutation_mutation on mutations(mutation_aa(8));

-- some editing
-- delete from mutations where sample_name = "Sample name";

-- some fixes
-- update mutations set mutation_aa = "p.E597A" where mutation_aa = "E597A";
update mutations set mutation_aa = "p.K120_F121insLYHKGLLK" where mutation_aa = "pK120_F121insLYHKGLLK";
update mutations set mutation_aa = "p.Q48_D54del" where mutation_aa = "pQ48_D54del";
update mutations set mutation_id = 0 where mutation_aa = '' and mutation_id != 0;

update mutations set primary_hist = hist_subtype where primary_hist = "other";

-- Remove all totally unknown mutations, even when we have a mutation identifier
-- as these are useless and unsafe for frequencies
delete from mutations where mutation_cds = 'c.?' and mutation_aa = 'p.?';
-- Query OK, 8412 rows affected (17.65 sec)

-- Replacing "other" in the primary_hist 
-- update mutations set primary_hist = hist_subtype where primary_hist = "other";

-- ========================================================================================
-- T R A N S I T I O N    T A B L E S 
-- 
-- These are the tables that manage the transition into the main data tables. They are basically
-- doing some intermediate calculations on sample sizes and frequencies. These tables can and 
-- should be temporary tables, as we never want to keep them. 
DROP TABLE mutated_hist;
DROP TABLE sample_hist;
DROP TABLE mutation_freqs;
DROP TABLE frequencies;

CREATE TABLE mutated_hist (
  gene_name char(30) ,
  mutation_aa char(80),
  mutation_cds char(80),
  mutation_id int,
  primary_site char(150),
  primary_hist char(150),
  mutated int,
  KEY primary_site (primary_site(20)),
  KEY gene_name (gene_name(8)),
  KEY mutation_aa (mutation_aa(10)),
  KEY primary_hist (primary_hist(20))
) ENGINE MyISAM;

CREATE TABLE sample_hist (
  gene_name char(30) ,
  primary_site char(150),
  primary_hist char(150),
  samples int ,
  KEY primary_site(primary_site(20)),
  KEY gene_name(gene_name(8)),
  KEY primary_hist (primary_hist(20))
) ENGINE MyISAM;

CREATE TABLE mutation_freqs (
  gene char(30),
  mutation char(80),
  mutation_cds char(80),
  mutation_id int,
  tumor_tissue char(150),
  histology_type char(150),
  total_sample int,
  sample_mutated int,
  frequency float,
  INDEX (gene(8)),
  INDEX (mutation(10)),
  INDEX (tumor_tissue(20)),
  INDEX (histology_type(20))
) ENGINE MyISAM;

CREATE TABLE frequencies (
  gene char(30),
  mutation char(80),
  mutation_cds char(80),
  mutation_id int,
  tumor_type char(150),
  total_sample int,
  sample_mutated int,
  frequency float,
  index (mutation(10)),
  index (tumor_type(20)),
  index (total_sample),
  index (frequency)
) ENGINE MyISAM;

insert into mutated_hist 
select gene_name, mutation_aa, mutation_cds, mutation_id, primary_site, primary_hist, count(distinct(id_sample)) 
from mutations 
where mutation_id != 0 
group by gene_name, mutation_aa, mutation_cds, mutation_id, primary_site, primary_hist;

insert into sample_hist 
select gene_name, primary_site, primary_hist, count(distinct(id_sample)) 
from mutations 
group by gene_name, primary_site, primary_hist;

insert into mutation_freqs 
select s.gene_name, m.mutation_aa, m.mutation_cds, m.mutation_id, s.primary_site, s.primary_hist, s.samples, m.mutated, m.mutated/s.samples
from mutated_hist as m, sample_hist as s 
where s.gene_name = m.gene_name 
and s.primary_site = m.primary_site 
and s.primary_hist = m.primary_hist;

insert into frequencies 
select gene, substring(mutation, 3) as mutation, mutation_cds, mutation_id, REPLACE(concat(tumor_tissue, ' ', histology_type), "_", " ")  as tumor_type, total_sample, sample_mutated, frequency 
from mutation_freqs;

create index frequencies_gene on frequencies(gene(8));


-- OK, at this stage we can continue with the frequency and various other updates. This needs to 
-- maintain integrity as far as poss. 

-- First of all, add any new genes

--- INSERT INTO gene (name)
--- SELECT DISTINCT UPPER(gene) AS "name" FROM tmp_frequencies 
--- WHERE gene NOT IN (SELECT name FROM gene);

-- Next, add any new mutations - this may require finding these genes

--- INSERT INTO mutation (gene_id, name) 
--- SELECT DISTINCT gene.id, concat(tmp_frequencies.gene, ' ', tmp_frequencies.mutation)
--- FROM tmp_frequencies
--- JOIN gene ON gene.name = tmp_frequencies.gene
--- LEFT JOIN mutation ex ON ex.gene_id = gene.id AND ex.name = concat(tmp_frequencies.gene, ' ', tmp_frequencies.mutation)
--- WHERE ex.id IS NULL;

--- UPDATE mutation
--- SET last_updated = now()
--- WHERE last_updated IS NULL;

--- INSERT INTO mutation_frequency (mutation_id, tumour_type_id, frequency, samples)
--- SELECT ex.id, tt.id, tmp_frequencies.frequency, tmp_frequencies.sample_mutated
--- FROM tmp_frequencies
--- JOIN gene ON gene.name = tmp_frequencies.gene
--- JOIN mutation ex ON ex.gene_id = gene.id AND ex.name = concat(tmp_frequencies.gene, ' ', tmp_frequencies.mutation)
--- JOIN tumour_type tt ON tmp_frequencies.tumor_type = tt.name
--- LEFT JOIN mutation_frequency mfx ON mfx.mutation_id = ex.id AND mfx.tumour_type_id = tt.id
--- WHERE mfx.mutation_id IS NULL;

-- ************************************************************************************************************
-- Now we can update the frequencies. 
--
-- UPDATE tmp_frequencies
-- STRAIGHT_JOIN gene ON gene.name = tmp_frequencies.gene
-- STRAIGHT_JOIN tumour_type tt ON tmp_frequencies.tumor_type = tt.name
-- LEFT JOIN mutation m ON m.gene_id = gene.id AND m.name = concat(gene.name, ' ', tmp_frequencies.mutation)
-- STRAIGHT_JOIN mutation_frequency mf ON mf.mutation_id = m.id AND mf.tumour_type_id = tt.id
-- SET mf.frequency = tmp_frequencies.frequency, mf.samples = tmp_frequencies.total_sample
-- WHERE ABS(mf.frequency - tmp_frequencies.frequency) > 0.00001 OR mf.samples <> tmp_frequencies.total_sample;

DELETE FROM testgps.known_gene_cosmic_genes;
DELETE FROM testgps.known_mutation_cosmic_mutations;
DELETE FROM testgps.known_tumour_type_cosmic_tumour_types;

DELETE FROM testgps.cosmic_mutation_frequency;
DELETE FROM testgps.cosmic_mutation;
DELETE FROM testgps.cosmic_gene;
DELETE FROM testgps.cosmic_tumour_type;

INSERT INTO testgps.cosmic_gene (name, cosmic_gene_id)
SELECT DISTINCT UPPER(gene) AS name, 0 as cosmic_gene_id FROM frequencies;

UPDATE testgps.cosmic_gene
SET cosmic_gene_id = id;

-- Next, let's update the mutations. Basically, this is the same as the above, with an additional
-- lookup against the gene just loaded.

INSERT INTO testgps.cosmic_mutation (name, gene_id, cosmic_mutation_id)
SELECT DISTINCT mutation AS name, testgps.cosmic_gene.cosmic_gene_id as gene_id, mutation_id as cosmic_mutation_id
FROM frequencies 
JOIN testgps.cosmic_gene ON testgps.cosmic_gene.name COLLATE latin1_general_ci = UPPER(frequencies.gene);

-- OK, tumour types next

INSERT INTO testgps.cosmic_tumour_type (name, cosmic_tumour_type_id)
SELECT DISTINCT tumor_type AS name, 0 as cosmic_tumour_type_id
FROM frequencies;

UPDATE testgps.cosmic_tumour_type
SET cosmic_tumour_type_id = id;

-- Now let's do the frequencies. This is simpler, as we don't need to work with the non COSMIC
-- tables

CREATE INDEX cosmic_mutation_id ON testgps.cosmic_mutation(cosmic_mutation_id);

INSERT INTO testgps.cosmic_mutation_frequency (frequency, mutation_id, samples, tumour_type_id, relevance)
SELECT DISTINCT frequencies.frequency, 
                testgps.cosmic_mutation.id, 
                frequencies.total_sample, 
                testgps.cosmic_tumour_type.id,
                (((ROUND(frequencies.frequency * frequencies.total_sample)) * 
                  (ROUND(frequencies.frequency * frequencies.total_sample))) / 
                 frequencies.total_sample) * 2 AS relevance
FROM frequencies
JOIN testgps.cosmic_gene ON testgps.cosmic_gene.name COLLATE latin1_general_ci = frequencies.gene
JOIN testgps.cosmic_mutation ON testgps.cosmic_mutation.cosmic_mutation_id = frequencies.mutation_id
JOIN testgps.cosmic_tumour_type ON testgps.cosmic_tumour_type.name COLLATE latin1_general_ci = frequencies.tumor_type
WHERE frequencies.total_sample >= 20
ORDER BY testgps.cosmic_tumour_type.name, testgps.cosmic_gene.name, testgps.cosmic_mutation.name;


-- Now rebuild associations
-- First, tumour types, with a little fuzziness
DELETE FROM testgps.known_tumour_type_cosmic_tumour_types;
INSERT INTO testgps.known_tumour_type_cosmic_tumour_types (known_tumour_type_id, cosmic_tumour_type_id)
SELECT testgps.known_tumour_type.id, testgps.cosmic_tumour_type.id
FROM testgps.known_tumour_type
JOIN testgps.cosmic_tumour_type ON REPLACE(SUBSTRING(testgps.known_tumour_type.name, 1, 48), "-", " ") = REPLACE(SUBSTRING(testgps.cosmic_tumour_type.name, 1, 48), "-", " ")
ORDER BY testgps.known_tumour_type.name ASC;

-- Next, genes, without any fuzziness
DELETE FROM testgps.known_gene_cosmic_genes;
INSERT INTO testgps.known_gene_cosmic_genes (known_gene_id, cosmic_gene_id)
SELECT testgps.known_gene.id, testgps.cosmic_gene.id
FROM testgps.known_gene
JOIN testgps.cosmic_gene ON testgps.known_gene.name = testgps.cosmic_gene.name
ORDER BY testgps.known_gene.name ASC;

-- And finally, let's do the mutations themselves

UPDATE testgps.known_mutation SET mutation = 'E746_T751>A'  WHERE mutation = 'E746_A750del, T751A';
UPDATE testgps.known_mutation SET mutation = 'E746_A750>V'  WHERE mutation = 'E746_A750del, V ins';
UPDATE testgps.known_mutation SET mutation = 'E746_T751>I'  WHERE mutation = 'E746_T751del, I ins';
UPDATE testgps.known_mutation SET mutation = 'E746_S752>D'  WHERE mutation = 'E746_T751del, S752D';
UPDATE testgps.known_mutation SET mutation = 'E746_T751>V'  WHERE mutation = 'E746_T751del, V ins';
UPDATE testgps.known_mutation SET mutation = 'L747_A750>P'  WHERE mutation = 'L747_E749del, A750P';
UPDATE testgps.known_mutation SET mutation = 'L747_P753>S'  WHERE mutation = 'L747_S752del, P753S';
UPDATE testgps.known_mutation SET mutation = 'L747_T751del' WHERE mutation = 'L747_T750del';
UPDATE testgps.known_mutation SET mutation = 'L747_T751>P'  WHERE mutation = 'L747_T750del, P ins';
UPDATE testgps.known_mutation SET mutation = 'L747_S752>Q'  WHERE mutation = 'L747_S752del, Q ins';

UPDATE testgps.known_mutation SET mutation = 'G776>LC'  WHERE mutation = 'G776LC';
UPDATE testgps.known_mutation SET mutation = 'G776>VC'  WHERE mutation = 'G776VC';

UPDATE testgps.known_mutation SET mutation = 'Q43X'  WHERE mutation = 'Q43X,stop';
UPDATE testgps.known_mutation SET mutation = 'E17K'  WHERE mutation = 'E17del,K missense';

UPDATE testgps.known_mutation
SET mutation = 'D594V', ref_allele = 'A', var_allele = 'T'
WHERE mutation = 'D594V|G' and gene = 'BRAF';

DELETE FROM testgps.known_mutation WHERE mutation = 'D594G';
INSERT INTO testgps.known_mutation (version, accession, chromosome, dbsnp, gene, guid, mutation, ncbi_reference, origin, public_id, ref_allele, start, stop, 
                                    var_allele, known_gene_id)
SELECT 1 as version, 'NM_004333' as accession, '7' as chromosome, NULL as dbsnp, 'BRAF' as gene, UUID() as guid, 'D594G' as mutation,
       NULL as ncbi_reference, 0 as origin, NULL as public_id, 'A' as ref_allele, 140453154 as start, 140453154 as stop, 
       'G' as var_allele, 956 as known_gene_id;

DELETE FROM testgps.known_mutation
WHERE mutation = 'S752I/F' and gene = 'EGFR';

-- OK, now we can finally rebuild the mutation associations. First, delete all the old ones

DELETE FROM testgps.known_mutation_cosmic_mutations;

INSERT INTO testgps.known_mutation_cosmic_mutations (known_mutation_id, cosmic_mutation_id)
SELECT testgps.known_mutation.id, testgps.cosmic_mutation.id
FROM testgps.known_mutation
JOIN testgps.known_gene ON testgps.known_gene.id = testgps.known_mutation.known_gene_id
JOIN testgps.cosmic_gene ON testgps.known_gene.name = testgps.cosmic_gene.name
JOIN testgps.cosmic_mutation ON testgps.cosmic_mutation.gene_id = testgps.cosmic_gene.id AND testgps.cosmic_mutation.name = testgps.known_mutation.mutation
WHERE testgps.cosmic_mutation.id IS NOT NULL;

DELETE FROM testgps.known_mutation WHERE guid = '49277517-7bf2-4e0e-b2b7-5242a5c9031c';
DELETE FROM testgps.known_mutation WHERE guid = '587de30b-037f-4b20-8d79-e7a78ffcc248';
DELETE FROM testgps.known_mutation WHERE guid = '3c590c59-3e70-40e7-beb1-65d1bfd79964';
DELETE FROM testgps.known_mutation WHERE guid = '719e0e2e-d0c5-41ff-b1ff-2a9139166660';
DELETE FROM testgps.known_mutation WHERE guid = 'd9761807-2a06-475f-bf0f-1aba598121f8';
DELETE FROM testgps.known_mutation WHERE guid = '6641015b-834d-4671-bd6d-93e4e6a3abd9';


-- SELECT testgps.known_mutation.id, testgps.known_mutation.mutation, testgps.cosmic_mutation.id, testgps.cosmic_mutation.name
-- FROM testgps.known_mutation
-- JOIN testgps.known_gene ON testgps.known_gene.id = testgps.known_mutation.known_gene_id
-- JOIN testgps.cosmic_gene ON testgps.known_gene.name = testgps.cosmic_gene.name
-- JOIN testgps.cosmic_mutation ON testgps.cosmic_mutation.gene_id = testgps.cosmic_gene.id AND testgps.cosmic_mutation.name = testgps.known_mutation.mutation
-- WHERE testgps.cosmic_mutation.id IS NOT NULL;

