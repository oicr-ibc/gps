#!/usr/bin/perl -w

use strict;
use warnings;
use feature qw(say);

use DBI;
use Carp;
use IO::Handle;
use XML::Writer;

my $dbh;
my $data = '~/Downloads/CosmicCompleteExport_v59_230512.tsv';

sub execute_sql {
	my ($statement) = @_;
	say STDERR "$statement";
	$dbh->do($statement) or die($dbh->errstr());
}

$dbh = DBI->connect("dbi:mysql:cosmic_data","root");

#load_phase();
#patch_phase();
#duplicates_phase();
#frequencies_phase();
output_phase();

$dbh->disconnect();

sub load_phase {
    execute_sql(<<__ENDSQL__);
alter database
    default character set 'latin1'
    default collate 'latin1_general_ci'
__ENDSQL__

    execute_sql(<<__ENDSQL__);
drop table if exists mutations
__ENDSQL__

    execute_sql(<<__ENDSQL__);
create table mutations (
  id int primary key auto_increment,
  sample_name varchar(40) not null,
  id_sample int,
  id_tumour int,
  primary_site varchar(100) not null,
  site_subtype varchar(250),
  primary_hist varchar(150),
  hist_subtype varchar(255),
  genome_wide_screen int,
  gene_name varchar(40),
  accession varchar(30),
  hgnc char(30),
  mutation_id int,
  mutation_cds varchar(180),
  mutation_aa varchar(80),
  mutation_description varchar(255),
  mutation_zygosity varchar(30),
  ncbi36_position varchar(30),
  ncbi36_strand char(1),
  grch37_position varchar(30),
  grch37_strand char(1),
  somatic_status varchar(50),
  pubmed char(20),
  sample_source varchar(20),
  tumour_origin varchar(50),
  comments varchar(1024),
  start int,
  stop int,
  chromosome char(2),
  var_allele varchar(64)
) ENGINE MyISAM;
__ENDSQL__

    execute_sql(<<__ENDSQL__);
load data local infile '$data'
into table mutations
ignore 1 lines
(gene_name, accession, hgnc, sample_name, id_sample, id_tumour, primary_site, site_subtype, primary_hist, hist_subtype, 
 \@genome_wide_screen, 
 \@mutation_id, 
 mutation_cds, mutation_aa, 
 mutation_description, mutation_zygosity, 
 ncbi36_position, ncbi36_strand, grch37_position, grch37_strand, 
 somatic_status, pubmed, 
 sample_source, tumour_origin, comments)
SET mutation_id = IF(\@mutation_id='',0,\@mutation_id), 
    genome_wide_screen = IF(\@genome_wide_screen='',0,IF(\@genome_wide_screen='y',1,0))
__ENDSQL__

    execute_sql(<<__ENDSQL__);
ALTER TABLE mutations
ADD INDEX mutation_sample_name(sample_name(8)),
ADD INDEX mutation_id_sample(id_sample),
ADD INDEX mutation_id_idx(mutation_id),
ADD INDEX mutation_gene_mutation(gene_name(8), mutation_aa(8)),
ADD INDEX mutation_mutation(mutation_aa(8))
__ENDSQL__

    execute_sql(<<__ENDSQL__);
UPDATE mutations
SET chromosome = SUBSTRING_INDEX(grch37_position, ':', 1),
    start = CONVERT(SUBSTRING_INDEX(SUBSTRING_INDEX(grch37_position, ':', -1), '-', 1), SIGNED INTEGER),
    stop = CONVERT(SUBSTRING_INDEX(SUBSTRING_INDEX(grch37_position, ':', -1), '-', -1), SIGNED INTEGER),
    var_allele = 
      CASE
        WHEN mutation_cds REGEXP '^c\\\\.[0-9_]+ins[ACGT]+\$' THEN SUBSTRING_INDEX(mutation_cds, 'ins', -1)
        WHEN mutation_cds REGEXP '^c\\\\.[0-9_]+del.*\$' THEN ''
        WHEN mutation_cds REGEXP '^c\\\\.[0-9_]+[ACGT]+>[ACGT]+\$' THEN SUBSTRING_INDEX(mutation_cds, '>', -1)
        ELSE NULL
      END
WHERE grch37_position IS NOT NULL AND grch37_position != ''
__ENDSQL__
};

sub patch_phase {
    execute_sql(<<__ENDSQL__);
update mutations set mutation_aa = "p.K120_F121insLYHKGLLK" where mutation_aa = "pK120_F121insLYHKGLLK"
__ENDSQL__

    execute_sql(<<__ENDSQL__);
update mutations set mutation_aa = "p.Q48_D54del" where mutation_aa = "pQ48_D54del"
__ENDSQL__

    execute_sql(<<__ENDSQL__);
update mutations set mutation_id = 0 where mutation_aa = '' and mutation_id != 0
__ENDSQL__

    execute_sql(<<__ENDSQL__);
update mutations set primary_hist = hist_subtype where primary_hist = "other"
__ENDSQL__

    execute_sql(<<__ENDSQL__);
update mutations set mutation_aa = 'p.?' where mutation_aa like 'p.0%'
__ENDSQL__

    execute_sql(<<__ENDSQL__);
update mutations set mutation_aa = 'p.?' where mutation_aa = 'p.unknown'
__ENDSQL__

    execute_sql(<<__ENDSQL__);
delete from mutations where mutation_cds like 'c.?%' and mutation_aa like 'p.?%'
__ENDSQL__
}

sub duplicates_phase {
	
    execute_sql(<<__ENDSQL__);
ALTER TABLE mutations
ADD INDEX mutations_duplicate_idx(grch37_position, mutation_cds(30), mutation_id),
ADD INDEX mutation_genomic_idx(chromosome, start, stop, var_allele(2))
__ENDSQL__

    execute_sql(<<__ENDSQL__);
DROP TEMPORARY TABLE IF EXISTS genomic_duplicates
__ENDSQL__
    
    # Just to make things really fun, we can't even trust the CDS, because that might
    # different for the same actual mutation. This is because the CDS is typically 
    # different in a variant.
        
    execute_sql(<<__ENDSQL__);
CREATE TEMPORARY TABLE genomic_duplicates
SELECT chromosome, start, stop, var_allele, COUNT(DISTINCT mutation_cds) AS count
FROM mutations
GROUP BY chromosome, start, stop, var_allele
HAVING COUNT(DISTINCT mutation_cds) > 1
AND var_allele IS NOT NULL
__ENDSQL__
    
    execute_sql(<<__ENDSQL__);
ALTER TABLE genomic_duplicates
ADD COLUMN mutation_cds VARCHAR(180)
__ENDSQL__
    
    execute_sql(<<__ENDSQL__);
UPDATE genomic_duplicates gd
SET gd.mutation_cds = 
  (SELECT m.mutation_cds FROM mutations m
   WHERE m.chromosome = gd.chromosome AND m.start = gd.start AND m.stop = gd.stop AND m.var_allele = gd.var_allele
   ORDER BY LENGTH(m.gene_name) ASC
   LIMIT 1)
__ENDSQL__
    
    execute_sql(<<__ENDSQL__);
UPDATE mutations m
JOIN genomic_duplicates gd ON m.chromosome = gd.chromosome AND m.start = gd.start AND m.stop = gd.stop AND m.var_allele = gd.var_allele
SET m.mutation_cds = gd.mutation_cds
__ENDSQL__
    
    execute_sql(<<__ENDSQL__);
DROP TEMPORARY TABLE IF EXISTS duplicates
__ENDSQL__
	
    # Query set to identify mutations with the same genomic coordinates but different
    # mutation identifiers, and convert them to use the same mutation identifier. 

    execute_sql(<<__ENDSQL__);
CREATE TEMPORARY TABLE duplicates
SELECT DISTINCT m.grch37_position, m.mutation_cds, 
       (SELECT x.gene_name FROM mutations x 
        WHERE x.grch37_position = m.grch37_position AND x.mutation_cds = m.mutation_cds 
        ORDER BY LENGTH(x.gene_name) ASC LIMIT 1) AS gene_name,
       (SELECT y.mutation_id FROM mutations y 
        WHERE y.grch37_position = m.grch37_position AND y.mutation_cds = m.mutation_cds 
        ORDER BY COUNT(DISTINCT sample_name) DESC LIMIT 1) as mutation_id,
       (SELECT z.mutation_aa FROM mutations z 
        WHERE z.grch37_position = m.grch37_position AND z.mutation_cds = m.mutation_cds 
        ORDER BY COUNT(DISTINCT sample_name) DESC LIMIT 1) as mutation_aa
FROM (SELECT DISTINCT grch37_position, mutation_cds FROM mutations
      WHERE mutation_id != 0
      AND grch37_position is not null
      AND grch37_position != ''
      AND mutation_cds not like 'c.?%'
      GROUP BY grch37_position, mutation_cds
      HAVING COUNT(distinct mutation_id) > 1) AS m
__ENDSQL__

    execute_sql(<<__ENDSQL__);
UPDATE mutations m 
JOIN duplicates dups ON dups.grch37_position = m.grch37_position AND dups.mutation_cds = m.mutation_cds
SET m.mutation_id = dups.mutation_id, m.gene_name = dups.gene_name, m.mutation_aa = dups.mutation_aa
WHERE m.mutation_id != 0
AND m.grch37_position IS NOT NULL
AND m.grch37_position != ''
__ENDSQL__
}

sub frequencies_phase {
    execute_sql("DROP TABLE mutated_hist");
    execute_sql("DROP TABLE sample_hist");
    execute_sql("DROP TABLE mutation_freqs");
    execute_sql("DROP TABLE frequencies");

    execute_sql(<<__ENDSQL__);
CREATE TABLE mutated_hist (
  gene_name char(30) ,
  mutation_aa char(80),
  primary_site char(150),
  primary_hist char(150),
  mutated int,
  KEY primary_site (primary_site(20)),
  KEY gene_name (gene_name(8)),
  KEY mutation_aa (mutation_aa(10)),
  KEY primary_hist (primary_hist(20))
) ENGINE MyISAM
__ENDSQL__

    execute_sql(<<__ENDSQL__);
CREATE TABLE sample_hist (
  gene_name char(30) ,
  primary_site char(150),
  primary_hist char(150),
  samples int ,
  KEY primary_site(primary_site(20)),
  KEY gene_name(gene_name(8)),
  KEY primary_hist (primary_hist(20))
) ENGINE MyISAM
__ENDSQL__

    execute_sql(<<__ENDSQL__);
CREATE TABLE mutation_freqs (
  gene char(30),
  mutation char(80),
  tumor_tissue char(150),
  histology_type char(150),
  total_sample int,
  sample_mutated int,
  frequency float,
  INDEX (gene(8)),
  INDEX (mutation(10)),
  INDEX (tumor_tissue(20)),
  INDEX (histology_type(20))
) ENGINE MyISAM
__ENDSQL__

    execute_sql(<<__ENDSQL__);
CREATE TABLE frequencies (
  gene char(30),
  mutation char(80),
  tumor_type char(150),
  total_sample int,
  sample_mutated int,
  frequency float,
  index (mutation(10)),
  index (tumor_type(20)),
  index (total_sample),
  index (frequency)
) ENGINE MyISAM
__ENDSQL__

    execute_sql(<<__ENDSQL__);
insert into mutated_hist 
select gene_name, mutation_aa, primary_site, primary_hist, count(distinct(id_sample)) 
from mutations 
where mutation_id != 0 
group by gene_name, mutation_aa, primary_site, primary_hist
__ENDSQL__

    execute_sql(<<__ENDSQL__);
insert into sample_hist 
select gene_name, primary_site, primary_hist, count(distinct(id_sample)) 
from mutations 
group by gene_name, primary_site, primary_hist
__ENDSQL__

    execute_sql(<<__ENDSQL__);
insert into mutation_freqs 
select s.gene_name, m.mutation_aa, s.primary_site, s.primary_hist, s.samples, m.mutated, m.mutated/s.samples
from mutated_hist as m, sample_hist as s 
where s.gene_name = m.gene_name 
and s.primary_site = m.primary_site 
and s.primary_hist = m.primary_hist
__ENDSQL__

    execute_sql(<<__ENDSQL__);
insert into frequencies 
select gene, substring(mutation, 3) as mutation, REPLACE(concat(tumor_tissue, ' ', histology_type), "_", " ")  as tumor_type, total_sample, sample_mutated, frequency 
from mutation_freqs
__ENDSQL__

    execute_sql(<<__ENDSQL__);
delete from frequencies 
where mutation = '?'
__ENDSQL__

    execute_sql(<<__ENDSQL__);
create index frequencies_gene on frequencies(gene(8))
__ENDSQL__

}

sub output_phase {
	my $output = IO::Handle->new_from_fd(\*STDOUT, "w");
	my $writer = XML::Writer->new(OUTPUT => $output, DATA_MODE => 1, DATA_INDENT => 2);

    execute_sql(<<__ENDSQL__);
CREATE TEMPORARY TABLE cosmic_gene (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name varchar(32) UNIQUE
) ENGINE MyISAM
__ENDSQL__

    execute_sql(<<__ENDSQL__);
CREATE TEMPORARY TABLE cosmic_tumour_type (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name varchar(150) UNIQUE
) ENGINE MyISAM
__ENDSQL__

    execute_sql(<<__ENDSQL__);
CREATE TEMPORARY TABLE cosmic_mutation (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name varchar(32),
  gene_id INT NOT NULL,
  UNIQUE KEY cosmic_mutation_mutation (gene_id, name)
) ENGINE MyISAM
__ENDSQL__

    execute_sql(<<__ENDSQL__);
INSERT INTO cosmic_gene (name)
SELECT DISTINCT UPPER(gene) AS name
FROM frequencies
__ENDSQL__

    execute_sql(<<__ENDSQL__);
INSERT INTO cosmic_tumour_type (name)
SELECT DISTINCT LOWER(tumor_type) AS name
FROM frequencies
__ENDSQL__
    
    execute_sql(<<__ENDSQL__);
INSERT INTO cosmic_mutation (name, gene_id)
SELECT DISTINCT mutation AS name, cosmic_gene.id as gene_id
FROM frequencies 
JOIN cosmic_gene ON cosmic_gene.name = UPPER(frequencies.gene)
__ENDSQL__

    my $mutations_statement = $dbh->prepare(<<__ENDSQL__) or die($dbh->errstr());
SELECT DISTINCT mutation_id, mutation_cds, grch37_position
FROM mutations
WHERE mutation_id != 0
AND gene_name = ? 
AND mutation_aa = ?
__ENDSQL__

    my $statement = $dbh->prepare(<<__ENDSQL__) or die($dbh->errstr());
SELECT DISTINCT cosmic_tumour_type.id,
                cosmic_tumour_type.name,
                cosmic_gene.id,
                cosmic_gene.name,
                cosmic_mutation.id, 
                cosmic_mutation.name, 
                frequencies.frequency, 
                frequencies.sample_mutated,
                frequencies.total_sample, 
                (((ROUND(frequencies.frequency * frequencies.total_sample)) * 
                  (ROUND(frequencies.frequency * frequencies.total_sample))) / 
                 frequencies.total_sample) * 2 AS relevance
FROM frequencies
JOIN cosmic_gene ON cosmic_gene.name = frequencies.gene
JOIN cosmic_mutation ON cosmic_mutation.name = frequencies.mutation AND cosmic_gene.id = cosmic_mutation.gene_id
JOIN cosmic_tumour_type ON cosmic_tumour_type.name = frequencies.tumor_type
WHERE frequencies.total_sample >= 20
ORDER BY cosmic_tumour_type.name, cosmic_gene.name, cosmic_mutation.name;
__ENDSQL__

    $writer->xmlDecl();
    $writer->startTag("frequencies");

    $statement->execute() or die($dbh->errstr());
    my $last_tumour_type_id = -1;
    my $last_gene_id = -1;
    while(my ($tumour_type_id, $tumour_type, $gene_id, $gene, $mutation_id, $mutation, $frequency, $mutated, $samples, $relevance) = $statement->fetchrow_array()) {
    	if ($tumour_type_id != $last_tumour_type_id) {
            $writer->endTag("gene") if ($writer->in_element('gene'));
    		$writer->endTag("tumour_type") if ($writer->in_element('tumour_type'));
    		$writer->startTag("tumour_type", name => $tumour_type);
    		$last_tumour_type_id = $tumour_type_id;
    		$last_gene_id = -1;
    	}
        if ($gene_id != $last_gene_id) {
            $writer->endTag("gene") if ($writer->in_element('gene'));
            $writer->startTag("gene", name => $gene);
            $last_gene_id = $gene_id;
        }
        
        $writer->startTag("mutation", name => $mutation);
        
        $mutations_statement->execute($gene, "p.$mutation") or die($dbh->errstr());
        while(my ($cosmic_id, $mutation_cds, $grch37_position) = $mutations_statement->fetchrow_array()) {
            if ($mutation_cds =~ m{^c\.(?<start>\d+)(?:_(?<stop>\d+))?(?:ins(?<insert>[ACGT]+)|del(?<delete>[ACGT]+)|(?<from>[ACGT]+)>(?<to>[ACGT]+))$}) {
                my $var_allele = $+{to} // $+{insert};
                my $ref_allele = $+{from} // $+{delete};
                $var_allele //= "" if (exists($+{delete}));
                $ref_allele //= "" if (exists($+{insert}));
                if (defined($var_allele) && defined($ref_allele) && $grch37_position =~ m{^(?<chromosome>\d+):(?<from>\d+)-(?<to>\d+)$}) {
                    my @attributes = (chromosome => $+{chromosome}, start => $+{from}, stop => $+{to});
                    push @attributes, ref_allele => $ref_allele;
                    push @attributes, var_allele => $var_allele;
                    push @attributes, cosmic_id => $cosmic_id if (defined($cosmic_id));
                    $writer->emptyTag('known', @attributes);
                }
            }
        }

        $writer->dataElement('frequency', $frequency);
    	$writer->dataElement('mutated', $mutated);
        $writer->dataElement('samples', $samples);
        $writer->dataElement('relevance', $relevance);
        $writer->endTag("mutation")
    }
    
    $writer->endTag("gene") if ($writer->in_element('gene'));
    $writer->endTag("tumour_type") if ($writer->in_element('tumour_type'));
    
    $writer->endTag("frequencies");
	$writer->end();
}